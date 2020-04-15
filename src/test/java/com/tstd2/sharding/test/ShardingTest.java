package com.tstd2.sharding.test;

import com.tstd2.sharding.namegenerator.DefaultDataSourceNameGenerator;
import com.tstd2.sharding.strategy.DefaultShardingStrategy;
import com.tstd2.sharding.namegenerator.DefaultTableNameGenerator;
import com.tstd2.sharding.namegenerator.DataSourceNameGenerator;
import com.tstd2.sharding.strategy.ShardingStrategy;
import com.tstd2.sharding.namegenerator.TableNameGenerator;
import org.junit.Test;

public class ShardingTest {

    private ShardingStrategy shardingStrategy = new DefaultShardingStrategy();

    private DataSourceNameGenerator dataSourceNameGenerator = new DefaultDataSourceNameGenerator();

    private TableNameGenerator tableNameGenerator = new DefaultTableNameGenerator();

    @Test
    public void testSharding() {
        // 表名前缀
        String tablePrefix = "user_";
        // 用于分表的字段值，比如用户ID
        String shardingValue = "10001";
        // 总表数量
        int shardingTableCount = 4;
        // 总库数量
        int shardingDBCount = 2;

        // 计算表号库号
        ShardingStrategy.TablePair pair = this.shardingStrategy.sharding(shardingValue, shardingTableCount, shardingDBCount);

        // 库号
        int dataSourceNo = pair.getDataSourceNo();
        // 表号
        int tableNo = pair.getTableNo();

        System.out.println("dataSourceNo=" + dataSourceNo);
        System.out.println("tableNo=" + tableNo);

        // 生成库名
        String dataSourceName = this.dataSourceNameGenerator.generate(dataSourceNo, shardingDBCount);

        // 生成表名
        String tableName = this.tableNameGenerator.generate(tablePrefix, tableNo, shardingTableCount);

        System.out.println("库名：" + dataSourceName);
        System.out.println("表名：" + tableName);
    }

}
