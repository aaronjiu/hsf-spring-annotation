package com.taobao.hsf.spring.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * {@link org.springframework.beans.factory.xml.NamespaceHandler} for the '<code>hsf</code>' namespace.
 * 
 * @author <a href="mailto:tonglin@taobao.com">tonglin</a>
 * @version 1.0
 * @since 2013-2-6
 */
public class HSFNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());
	}

}
