package com.tstd2.sharding;

import com.tstd2.sharding.strategy.ShardingStrategy;

/**
 * 分库分表配置信息
 *
 * @author yancey
 * @date 2020/4/16 00:13
 */
public class ShardingContext {

    private String[] tablePrefixs;
    private String property;
    private ShardingStrategy shardingStrategy;

    public ShardingContext(String[] tablePrefixs, String property, ShardingStrategy shardingStrategy) {
        this.tablePrefixs = tablePrefixs;
        this.property = property;
        this.shardingStrategy = shardingStrategy;
    }

    public String[] getTablePrefixs() {
        return tablePrefixs;
    }

    public void setTablePrefixs(String[] tablePrefixs) {
        this.tablePrefixs = tablePrefixs;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public ShardingStrategy getShardingStrategy() {
        return shardingStrategy;
    }

    public void setShardingStrategy(ShardingStrategy shardingStrategy) {
        this.shardingStrategy = shardingStrategy;
    }
}
