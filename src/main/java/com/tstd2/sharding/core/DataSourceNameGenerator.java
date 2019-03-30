package com.tstd2.sharding.core;

/**
 * Sharding数据源实例名称生成器。
 */
public interface DataSourceNameGenerator {

    /**
     * 根据数据源号生成数据源名称。
     *
     * @param dataSourceNo    数据源号。
     * @param shardingDBCount 分库总数。
     * @return 生成的数据源名称。
     */
    String generate(int dataSourceNo, int shardingDBCount);

}
