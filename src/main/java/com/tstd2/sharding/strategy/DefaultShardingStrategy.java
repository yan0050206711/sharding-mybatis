package com.tstd2.sharding.strategy;

/**
 * 默认的拆分策略实现。
 */
public class DefaultShardingStrategy implements ShardingStrategy {

    @Override
    public <V> TablePair sharding(V shardingValue, int shardingTableCount, int shardingDBCount) {

        /**
         * 取分表字段的散列值
         */
        int hash = shardingValue.toString().hashCode();
        if (hash < 0) {
            hash = Math.abs(hash);
        }

        /**
         * hash模分表总数得表号
         */
        int shardingTableNo = hash % shardingTableCount;
        /**
         * 分表总数除以DB数得每个DB的表数
         */
        int tableCountEachDB = shardingTableCount / shardingDBCount;
        /**
         * 表号除以每个DB表数得库号
         */
        int shardingDBNo = shardingTableNo / tableCountEachDB;

        return TablePair.pair(shardingTableNo, shardingDBNo);
    }

}
