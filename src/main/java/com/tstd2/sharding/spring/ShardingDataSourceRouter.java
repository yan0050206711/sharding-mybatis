package com.tstd2.sharding.spring;

import com.tstd2.sharding.DataSourceLocalKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

/**
 * 支持分库分表数据源。
 */
public class ShardingDataSourceRouter extends AbstractRoutingDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShardingDataSourceRouter.class);

	@Override
	protected Object determineCurrentLookupKey() {
        try {
            //通过具体分库分表策略计算得出的分库号来获取一个数据源组。
            String groupKey = DataSourceLocalKeys.CURRENT_DS_GROUP_KEY.get();
            if (groupKey == null) {
                throw new IllegalStateException("找不到数据源组key=[" + groupKey + "]对应的数据源组!");
            }
            return groupKey;

        } catch (Exception e) {
            LOGGER.error("选择数据源过程中发生错误!", e);
            throw new DataSourceLookupFailureException(e.getMessage(), e);
        }
	}

}
