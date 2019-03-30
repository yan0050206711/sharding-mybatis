package com.tstd2.sharding;

/**
 * DataSource局部相关的一些Key值。
 */
public class DataSourceLocalKeys {
	
	/**
	 * 存储数据源组Key的线程变量。
	 */
	public static final ThreadLocal<String> CURRENT_DS_GROUP_KEY = new ThreadLocal<String>();
	
	private DataSourceLocalKeys(){}

}
