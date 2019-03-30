package com.tstd2.sharding;

/**
 * 分表信息。
 */
public class ShardingTableInfo {

	/**
	 * 数据表前缀。
	 */
	private String tablePrefix;
	
	/**
	 * 分表总数。
	 */
	private int shardingTableCount;
	
	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public int getShardingTableCount() {
		return shardingTableCount;
	}

	public void setShardingTableCount(int shardingTableCount) {
		this.shardingTableCount = shardingTableCount;
	}

}
