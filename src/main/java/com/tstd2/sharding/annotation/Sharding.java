/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.tstd2.sharding.annotation;

import com.tstd2.sharding.strategy.DefaultShardingStrategy;
import com.tstd2.sharding.strategy.ShardingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于支持分库分表、读写分离的注解。
 * <p>这个注解用在Dao的方法上，注解中需要指定分表前缀、分表属性、拆分策略、读写标志等。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Sharding {

	/**
	 * 分表的表前缀，sql中一般直接将这个前缀作为逻辑表名。
	 */
	String[] tablePrefix() default "";
	
	/**
	 * 分表的属性名称。
	 * <p>注意不是数据库字段。
	 */
	String property();

    /**
     * 分表策略
     */
	Class<? extends ShardingStrategy> strategy() default DefaultShardingStrategy.class;
	
}
