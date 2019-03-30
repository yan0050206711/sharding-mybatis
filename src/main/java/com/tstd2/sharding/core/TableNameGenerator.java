package com.tstd2.sharding.core;


/**
 * 分表表名处理器。
 */
public interface TableNameGenerator {

    /**
     * 生成实际的表名。
     *
     * @param tablePrefix        表前缀。
     * @param shardingTableNo    分表号。
     * @param shardingTableCount 分表总数。
     * @return 实际的物理表名。
     */
    String generate(String tablePrefix, int shardingTableNo, int shardingTableCount);

}
