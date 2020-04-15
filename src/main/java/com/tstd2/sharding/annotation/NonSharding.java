/*
 * Copyright (c) 2016-2020 LEJR.COM All Right Reserved
 */

package com.tstd2.sharding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于支持非分库分表，但需要读写分离和手工指定数据源的注解。
 * <p>这个注解用在Dao的方法上，注解中可以指定读写标志和手工数据源组key。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface NonSharding {

}
