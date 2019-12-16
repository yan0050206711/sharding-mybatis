package com.tstd2.sharding.core;

/**
 * 指定表号的查询策略
 *
 * @author yancey
 * @date 2019/12/16 23:00
 */
public class SpecifyShardingStrategy implements ShardingStrategy {
    @Override
    public <V> TablePair sharding(V shardingValue, int shardingTableCount, int shardingDBCount) {
        if (!(shardingValue instanceof Integer)) {
            throw new IllegalArgumentException("invalid shardingValue:" + shardingValue);
        }
        if (shardingTableCount <= 0) {
            throw new IllegalArgumentException("invalid shardingTableCount:" + shardingTableCount);
        }
        if (shardingTableCount < shardingDBCount) {
            throw new IllegalArgumentException("invalid shardingTableCount:" + shardingTableCount + ", shardingDBCount:" + shardingDBCount);
        }

        int tableNo = (Integer) shardingValue;
        if (tableNo <= 0) {
            throw new IllegalArgumentException("invalid shardingValue:" + shardingValue);
        }

        int dataSourceNo = tableNo / (shardingDBCount / shardingDBCount);

        return TablePair.pair(tableNo, dataSourceNo);
    }
}
