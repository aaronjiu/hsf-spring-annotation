package com.taobao.hsf.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于初始化<code>com.taobao.hsf.app.spring.util.HSFSpringConsumerBean</code>
 * 
 * @author <a href="mailto:tonglin@taobao.com">tonglin</a>
 * @version 1.0
 * @since 2013-2-6
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HSF {

	/**
	 * HSFSpringConsumerBean的bean Id
	 */
	String name() default "";

	/**
	 * 调用的服务的目标地址
	 */
	String target() default "";

	/**
	 * 调用的服务的版本
	 */
	String version();

	/**
	 * 方法单独配置超时(单位ms)，这样接口中的方法可以采用不同的超时时间， 格式：<tt>{"methodName1:clientTimeout1","methodName2:clientTimeout2"}</tt>
	 * 例如：methodSpecials= {"sum:2000"}
	 */
	String[] methodSpecials() default {};

	/**
	 * 暂不支持改配置
	 */
	String[] asyncallMethods() default {};

	/**
	 * 调用的服务所属的组
	 */
	String group() default "";
}
