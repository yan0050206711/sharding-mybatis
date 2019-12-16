package com.tstd2.sharding;

/**
 * 分表信息Bean。
 * <p>注：不同的虚拟数据源中不能出现相同的表前缀。
 */
public class TableShardingBean {

	/**
	 * 表前缀。
	 */
	private String tablePrefix;
	
	/**
	 * 分表总数。
	 */
	private int shardingTableCount;
	
	/**
	 * 分库总数。
	 */
	private int shardingDBCount;
	
	public TableShardingBean(String tablePrefix, int shardingTableCount, int shardingDBCount) {
		super();
		this.tablePrefix = tablePrefix;
		this.shardingTableCount = shardingTableCount;
		this.shardingDBCount = shardingDBCount;
	}

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

	public int getShardingDBCount() {
		return shardingDBCount;
	}

	public void setShardingDBCount(int shardingDBCount) {
		this.shardingDBCount = shardingDBCount;
	}

    @Override
    public String toString() {
        return "TableShardingBean{" +
                "tablePrefix='" + tablePrefix + '\'' +
                ", shardingTableCount=" + shardingTableCount +
                ", shardingDBCount=" + shardingDBCount +
                '}';
    }
}
