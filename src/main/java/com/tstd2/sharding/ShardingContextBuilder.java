package com.tstd2.sharding;

import com.tstd2.sharding.annotation.NonSharding;
import com.tstd2.sharding.annotation.Sharding;
import com.tstd2.sharding.annotation.ShardingTable;
import com.tstd2.sharding.strategy.DefaultShardingStrategy;
import com.tstd2.sharding.strategy.ShardingStrategy;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yancey
 * @date 2020/4/18 21:51
 */
public class ShardingContextBuilder {

    /**
     * 默认拆分策略。
     */
    private static final ShardingStrategy DEFAULT_SHARDING_STRATEGY= new DefaultShardingStrategy();

    private static final Map<Class<? extends  ShardingStrategy>, ShardingStrategy> SHARDING_STRATEGY_MAP = new ConcurrentHashMap<>();

    public static ShardingContext buildShardingContext(Class<?> classObject, String methodName) throws Exception {
        /*
         * 通过方法名称查找方法实例。
         * 注意这里有一个问题，没办法处理重载方法。
         */
        Class<?>[] paramTypes = null;//无法获取实际方法参数列表，这里传null。
        Method method = ReflectionUtils.findMethod(classObject, methodName, paramTypes);

        //根据方法上的@NonSharding注解进行逻辑处理。
        NonSharding nonShardingMeta = method.getAnnotation(NonSharding.class);
        if (nonShardingMeta != null) {
            return null;
        }

        //根据类上的@NonSharding注解进行逻辑处理。
        NonSharding nonShardingTableMeta = classObject.getAnnotation(NonSharding.class);
        if (nonShardingTableMeta != null) {
            return null;
        }

        String[] tablePrefixs = null;
        String property = null;
        Class<? extends ShardingStrategy> strategyClass = null;

        //根据方法上的@Sharding注解进行逻辑处理。
        Sharding shardingMeta = method.getAnnotation(Sharding.class);
        if (shardingMeta != null) {
            tablePrefixs = shardingMeta.tablePrefix();
            property = shardingMeta.property();
            strategyClass = shardingMeta.strategy();
        }

        //再尝试获取类上面的ShardingTable注解。
        ShardingTable shardingTableMeta = classObject.getAnnotation(ShardingTable.class);
        if (shardingTableMeta != null) {
            if (tablePrefixs == null || tablePrefixs.length == 0) {
                tablePrefixs = shardingTableMeta.tablePrefix();
            }
            if (StringUtils.isEmpty(property)) {
                property = shardingTableMeta.property();
            }
            if (strategyClass == null) {
                strategyClass = shardingTableMeta.strategy();
            }
        }

        if (tablePrefixs == null || tablePrefixs.length == 0) {
            String msg = String.format("必须在方法[%s]上的@Sharding中或者类[%s]上的@ShardingTable中指定tablePrefix!", methodName, classObject);
            throw new IllegalArgumentException(msg);
        }

        if (StringUtils.isEmpty(property)) {
            String msg = String.format("方法[%s]上的@Sharding必须指定property!", methodName);
            throw new IllegalArgumentException(msg);
        }

        if (strategyClass == null || DefaultShardingStrategy.class.equals(strategyClass)) {
            return new ShardingContext(tablePrefixs, property, DEFAULT_SHARDING_STRATEGY);
        }

        return new ShardingContext(tablePrefixs, property, getShardingStrategy(strategyClass));

    }

    private static ShardingStrategy getShardingStrategy(Class<? extends ShardingStrategy> strategyClass) throws IllegalAccessException, InstantiationException {
        ShardingStrategy strategy = SHARDING_STRATEGY_MAP.get(strategyClass);
        if (strategy == null) {
            strategy = strategyClass.newInstance();
            SHARDING_STRATEGY_MAP.put(strategyClass, strategy);
        }
        return strategy;
    }

}
