package com.tstd2.sharding.mybatis;

import com.tstd2.sharding.DataSourceLocalKeys;
import com.tstd2.sharding.namegenerator.DefaultDataSourceNameGenerator;
import com.tstd2.sharding.strategy.DefaultShardingStrategy;
import com.tstd2.sharding.namegenerator.DefaultTableNameGenerator;
import com.tstd2.sharding.namegenerator.DataSourceNameGenerator;
import com.tstd2.sharding.strategy.ShardingStrategy;
import com.tstd2.sharding.namegenerator.TableNameGenerator;
import com.tstd2.sharding.TableShardingBean;
import com.tstd2.sharding.TableShardingHolder;
import com.tstd2.sharding.annotation.NonSharding;
import com.tstd2.sharding.annotation.Sharding;
import com.tstd2.sharding.annotation.ShardingTable;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 针对Mybatis的分库分表插件。
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),})
public class ShardingInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShardingInterceptor.class);

    /**
     * 默认拆分策略。
     */
    private static final ShardingStrategy DEFAULT_SHARDINGSTRATEGY = new DefaultShardingStrategy();

    /**
     * 分表表名处理器。
     */
    private TableNameGenerator tableNameGenerator = new DefaultTableNameGenerator();

    /**
     * 数据源名称生成器。
     */
    private DataSourceNameGenerator dataSourceNameGenerator = new DefaultDataSourceNameGenerator();

    private static final ObjectFactory OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final ReflectorFactory REFLECTOR_FACTORY = new DefaultReflectorFactory();

    /**
     * 下面这个属性用于在Executor代理和StatementHandler代理之间传递分表信息。
     * 由于在处理这两个代理的时候，都会重新从Sqlsource中来构建BoundSql，也无法修改Sqlsource这份源数据，所以这里通过ThreadLocal来做这个传递。
     */
    private static final ThreadLocal<Map<String, String>> LOCAL_REALTABLE_MAPPING = new ThreadLocal<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object targetObject = invocation.getTarget();
        /**
         * 这里主要是做出数据源的选择
         */
        if (targetObject instanceof Executor) {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            Object parameterObject = invocation.getArgs()[1];
            //获取Dao执行的方法信息。 例如：com.a.b.UserDao.addUser
            String methodInfo = mappedStatement.getId();
            //获取Dao层面的目标执行方法。
            int splitIndex = methodInfo.lastIndexOf(".");
            String className = methodInfo.substring(0, splitIndex);
            String methodName = methodInfo.substring(splitIndex + 1);
            Class<?> classObject = Class.forName(className);
            /*
             * 通过方法名称查找方法实例。
             * 注意这里有一个问题，没办法处理重载方法。
             */
            Class<?>[] paramTypes = null;//无法获取实际方法参数列表，这里传null。
            Method method = ReflectionUtils.findMethod(classObject, methodName, paramTypes);
            //根据方法上的@Sharding注解进行逻辑处理。
            Sharding shardingMeta = method.getAnnotation(Sharding.class);
            if (shardingMeta != null) {
                String[] tablePrefixs = shardingMeta.tablePrefix();
                if (tablePrefixs == null || tablePrefixs.length == 0) {
                    //再尝试获取类上面的ShardingTable注解。
                    ShardingTable shardingTableMeta = classObject.getAnnotation(ShardingTable.class);
                    if (shardingTableMeta != null) {
                        tablePrefixs = shardingTableMeta.tablePrefix();
                    }
                }
                if (tablePrefixs == null || tablePrefixs.length == 0) {
                    LOGGER.error("必须在方法[{}]上的@Sharding中或者类[{}]上的@ShardingTable中指定tablePrefix!", method, classObject);
                    throw new IllegalArgumentException("tablePrefix can't be null");
                }
                String property = shardingMeta.property();
                if (property == null || property.trim().length() == 0) {
                    LOGGER.error("方法[{}]上的@Sharding必须指定property!", method);
                    throw new IllegalArgumentException("property can't be null");
                }

                // 获取保存Sql语句的对象
                BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
                //1.通过Sharding值来计算分表号和分库号。
                //1.1计算Sharding值。
                Object shardingValue = computeShardingValue(property, boundSql);

                // 循环获取每一个真实表名
                Map<String, String> realTableMap = new HashMap<>(tablePrefixs.length);
                for (int i = 0; i < tablePrefixs.length; i++) {
                    //获取分表元信息。
                    TableShardingBean shardingBean = TableShardingHolder.getTableShardingInfos().get(tablePrefixs[i]);
                    if (shardingBean == null) {
                        LOGGER.error("没有表[{}]对应的分表信息!", tablePrefixs[i]);
                        throw new IllegalArgumentException("shardinginfo can't be null");
                    }
                    LOGGER.debug("针对于[{}]的分表信息为[{}]！", tablePrefixs[i], shardingBean);
                    //1.2计算分表号和分库号。
                    //计算分表号和分库号。
                    int shardingTableCount = shardingBean.getShardingTableCount();
                    int shardingDBCount = shardingBean.getShardingDBCount();
                    ShardingStrategy.TablePair pair = DEFAULT_SHARDINGSTRATEGY.sharding(shardingValue, shardingTableCount, shardingDBCount);
                    LOGGER.debug("根据分库分表值[{}]算出来的库表号信息为:[{}]", shardingValue, pair);

                    //2.1生成实际物理表名。
                    String realTableName = tableNameGenerator.generate(tablePrefixs[i], pair.getTableNo(), shardingTableCount);
                    //2.2将表前缀和实际物理表名存到线程上下文。
                    realTableMap.put(tablePrefixs[i], realTableName);

                    // 业务要保证一个sql多个表需要在一个数据源中，所以此处只使用第一个表的数据源即可
                    if (i == 0) {
                        //3.通过分库号指定数据源
                        String dsGroupKey = dataSourceNameGenerator.generate(pair.getDataSourceNo(), shardingDBCount);
                        DataSourceLocalKeys.CURRENT_DS_GROUP_KEY.set(dsGroupKey);
                    }

                }
                // 最后将表明映射设置到上下文变量中
                LOCAL_REALTABLE_MAPPING.set(realTableMap);

                return invocation.proceed();
            } else {
                //根据方法上的@NonSharding注解进行逻辑处理。
                NonSharding nonShardingMeta = method.getAnnotation(NonSharding.class);

            }
        }

        /**
         * 这里主要是替换SQL中的模板
         */
        if (targetObject instanceof StatementHandler) {
            //拦截StatementHandler做分表。
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, OBJECT_FACTORY, OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);

            // 分离代理对象链(由于目标类可能被多个拦截器拦截，从而形成多次代理，通过下面的两次循环可以分离出最原始的的目标类)
            while (metaStatementHandler.hasGetter("h")) {
                Object object = metaStatementHandler.getValue("h");
                metaStatementHandler = MetaObject.forObject(object, OBJECT_FACTORY, OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);
            }
            // 分离最后一个代理对象的目标类
            while (metaStatementHandler.hasGetter("target")) {
                Object object = metaStatementHandler.getValue("target");
                metaStatementHandler = MetaObject.forObject(object, OBJECT_FACTORY, OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);
            }

            // 获取原始sql
            BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");
            String originalSql = boundSql.getSql();
            LOGGER.debug("originalSql = [{}]", originalSql);
            Map<String, String> realTableMap = LOCAL_REALTABLE_MAPPING.get();
            LOCAL_REALTABLE_MAPPING.remove();
            if (CollectionUtils.isEmpty(realTableMap)) {
                LOGGER.debug("no tablePrefix or realTableName in threadlocal!");
                return invocation.proceed();
            } else {
                // 替换sql
                String newSql = originalSql;
                for (Map.Entry<String, String> entry : realTableMap.entrySet()) {
                    newSql = newSql.replaceAll(entry.getKey(), entry.getValue());
                }
                LOGGER.debug("newSql = [{}]", newSql);
                metaStatementHandler.setValue("delegate.boundSql.sql", newSql);
                return invocation.proceed();
            }
        }
        return invocation.proceed();
    }

    /**
     * 计算用于分表的字段值
     */
    private Object computeShardingValue(String property, BoundSql boundSql) {
        List<ParameterMapping> mappings = boundSql.getParameterMappings();
        Object parameterObject = boundSql.getParameterObject();
        Class<?> clazz = parameterObject.getClass();
        Object propertyValue = null;
        if (clazz == String.class
                || clazz == Integer.class
                || clazz == int.class
                || clazz == Long.class
                || clazz == long.class
                || clazz == Short.class
                || clazz == short.class
                || clazz == Byte.class
                || clazz == byte.class
                || clazz == Double.class
                || clazz == double.class
                || clazz == Float.class
                || clazz == float.class) {
            propertyValue = parameterObject;
        } else if (clazz.equals(Array.class)) {
            for (int i = 0; i < mappings.size(); i++) {
                ParameterMapping mapping = mappings.get(i);
                String columnName = mapping.getProperty();
                if (property.equals(columnName)) {
                    propertyValue = Array.get(parameterObject, i);
                    break;
                }
            }
        } else {
            //FIXME 目前先简单实现，没考虑复杂情况。
            MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
            propertyValue = metaObject.getValue(property);
        }

        if (propertyValue == null) {
            LOGGER.error("无法获取参数[{}]的值!", property);
            throw new RuntimeException("can't get the value of " + property);
        }

        return propertyValue;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        } else if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {
        //do nothing！
    }

    public void setTableNameGenerator(TableNameGenerator tableNameGenerator) {
        this.tableNameGenerator = tableNameGenerator;
    }

    public void setDataSourceNameGenerator(
            DataSourceNameGenerator dataSourceNameGenerator) {
        this.dataSourceNameGenerator = dataSourceNameGenerator;
    }

}
