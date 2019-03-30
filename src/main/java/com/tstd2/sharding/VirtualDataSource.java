package com.tstd2.sharding;

import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * 虚拟数据源。
 */
public class VirtualDataSource implements InitializingBean {
	
	/**
	 * 分库数量。
	 * <p>等于数据源组个数。
	 */
	private int shardingDBCount;
	
	/**
	 * 分表信息。
	 */
	private List<ShardingTableInfo> shardingTableInfos;
	
	public int getShardingDBCount() {
		return shardingDBCount;
	}

    public void setShardingDBCount(int shardingDBCount) {
        this.shardingDBCount = shardingDBCount;
    }

    public List<ShardingTableInfo> getShardingTableInfos() {
		return shardingTableInfos;
	}

	public void setShardingTableInfos(List<ShardingTableInfo> shardingTableInfos) {
		this.shardingTableInfos = shardingTableInfos;
	}

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化分表信息
        TableShardingHolder.initTableShardingInfo(this);
    }
}
