/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.tstd2.sharding.strategy;

/**
 * 只分库不分表的拆分策略实现。
 *
 * @author yancey
 * @date 2020/04/15 23:29
 */
public class SplitDBNonTBShardingStrategy extends DefaultShardingStrategy implements ShardingStrategy {

    @Override
    public <V> TablePair sharding(V shardingValue,
                                  int shardingTableCount, int shardingDBCount) {

        TablePair tablePair = super.sharding(shardingValue, shardingTableCount, shardingDBCount);

        // 只分库不分表，则库号和表号是一样的，0号库0号表，1号库1号表
        int shardingTableNo = tablePair.getDataSourceNo();
        int shardingDBNo = tablePair.getDataSourceNo();
        return TablePair.pair(shardingTableNo, shardingDBNo);
    }

}
