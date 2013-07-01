package com.taobao.hsf.spring.annotation;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 用于初始化<code>com.taobao.hsf.app.spring.util.HSFSpringProvierBean</code>
 * 
 * @author <a href="mailto:tonglin@taobao.com">tonglin</a>
 * @version 1.0
 * @since 2013-2-6
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface HSFExport {

	/**
	 * 同serviceInterface
	 */
	String value() default "";

	/**
	 * 服务的接口
	 */
	Class<?> serviceInterface() default Serializable.class;

	/**
	 * 服务的目标
	 */
	String target() default "";

	/**
	 * 服务的版本
	 */
	String serviceVersion() default "";

	/**
	 * 服务的名称
	 */
	String serviceName() default "";

	/**
	 * 服务的描述
	 */
	String serviceDesc() default "";

	/**
	 * 服务所属的组别
	 */
	String serviceGroup() default "";

	/**
	 * 是否支持异步调用，默认不支持
	 */
	boolean supportAsynCall() default false;

	/**
	 * 默认客户端调用超时时间：3000ms
	 */
	int clientTimeout() default 3000;

	/**
	 * 默认的客户端连接空闲超时时间：10s
	 */
	int clientIdleTimeout() default 10;

	/**
	 * 方法单独配置超时(单位ms)，这样接口中的方法可以采用不同的超时时间，例如：methodSpecials= {"sum:2000"}
	 */
	String[] methodSpecials() default {};

	/**
	 * 序列化类型，默认为HESSIAN
	 */
	SerializeType serializeType() default SerializeType.java;

	/**
	 * 
	 */
	String methodToInjectConsumerIp() default "";

	/**
	 * 序列化类型
	 */
	enum SerializeType {
		java, hessian, hessian2
	}
}
