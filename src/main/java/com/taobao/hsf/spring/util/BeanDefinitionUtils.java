package com.taobao.hsf.spring.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * {@link BeanDefinition} Utils
 * 
 * @author <a href="mailto:tonglin@taobao.com">tonglin</a>
 * @version 1.0
 * @since 2013-2-19
 */
public class BeanDefinitionUtils {

	/**
	 * Add a string property value
	 * 
	 * @param builder the builder to add property to
	 * @param name the name of the property to add the property to
	 * @param value the value of the property to add the property to
	 */
	public static void addPropertyValue(BeanDefinitionBuilder builder, String name, String value) {
		addPropertyValue(builder, name, value, null);
	}

	/**
	 * Add a string property value
	 * 
	 * @param builder the builder to add property to
	 * @param name the name of the property to add the property to
	 * @param value the value of the property to add the property to
	 * @param defaultValue the value of the property to add the property to
	 */
	public static void addPropertyValue(BeanDefinitionBuilder builder, String name, String value, String defaultValue) {
		String _value = StringUtils.hasText(value) ? value : defaultValue;
		if (StringUtils.hasText(_value)) {
			builder.addPropertyValue(name, new TypedStringValue(_value));
		}
	}

	/**
	 * Add a string property value
	 * 
	 * @param builder the builder to add property to
	 * @param name the name of the property to add the property to
	 * @param value the value of the property to add the property to
	 */
	public static void addPropertyValue(BeanDefinitionBuilder builder, String name, int value) {
		addPropertyValue(builder, name, value, -1);
	}

	/**
	 * Add a string property value
	 * 
	 * @param builder the builder to add property to
	 * @param name the name of the property to add the property to
	 * @param value the value of the property to add the property to
	 * @param defaultValue the value of the property to add the property to
	 */
	public static void addPropertyValue(BeanDefinitionBuilder builder, String name, int value, int defaultValue) {
		int _value = -1 != value ? value : defaultValue;
		if (-1 != _value) {
			builder.addPropertyValue(name, new TypedStringValue(String.valueOf(_value)));
		}
	}

	/**
	 * Add a string property value
	 * 
	 * @param builder the builder to add property to
	 * @param name the name of the property to add the property to
	 * @param value the value of the property to add the property to
	 */
	public static void addPropertyValue(BeanDefinitionBuilder builder, String name, boolean value) {
		addPropertyValue(builder, name, String.valueOf(value));
	}

	/**
	 * Add a list property value
	 * 
	 * @param builder the builder to add property to
	 * @param name the name of the property to add the property to
	 * @param value the value of the property to add the property to
	 */
	public static void addPropertyValue(BeanDefinitionBuilder builder, String name, ManagedList<BeanDefinition> value) {
		if (!CollectionUtils.isEmpty(value)) {
			builder.addPropertyValue(name, value);
		}
	}

	/**
	 * Add a reference to the specified bean name under the property specified.
	 * 
	 * @param builder the builder to add reference to
	 * @param name the name of the property to add the reference to
	 * @param beanName the name of the bean being referenced
	 */
	public static void addPropertyReference(BeanDefinitionBuilder builder, String name, String beanName) {
		if (StringUtils.hasText(beanName)) {
			builder.addPropertyReference(name, beanName);
		}
	}

}
