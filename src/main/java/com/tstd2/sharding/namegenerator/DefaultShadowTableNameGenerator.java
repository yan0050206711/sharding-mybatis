/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.tstd2.sharding.namegenerator;

/**
 * 用于线上测试的影子表名处理器。
 * <p>这个处理器的逻辑是将分表名称替换为影子表。
 * <p>比如原始表是user_0004,那么影子表就是test_user_0004。
 *
 * @author WuHong
 * @version 1.0
 * @date 2016年4月12日
 */
public class DefaultShadowTableNameGenerator implements TableNameGenerator {

    private static final String SHADOWTABLE_PREFIX = "test_";

    private TableNameGenerator tableNameGenerator;

    public DefaultShadowTableNameGenerator(TableNameGenerator tableNameGenerator) {
        this.tableNameGenerator = tableNameGenerator;
    }

    @Override
    public String generate(String tablePrefix, int shardingTableNo, int shardingTableCount) {
        StringBuilder builder = new StringBuilder();
        builder.append(SHADOWTABLE_PREFIX);
        builder.append(tableNameGenerator.generate(tablePrefix, shardingTableNo, shardingTableCount));
        return builder.toString();
    }

}
