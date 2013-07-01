package com.taobao.hsf.spring.config;

import static com.taobao.hsf.spring.util.BeanDefinitionUtils.addPropertyValue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser for the 'annotation-driven' element of the 'hsf' namespace.
 * 
 * @author <a href="mailto:tonglin@taobao.com">tonglin</a>
 * @version 1.0
 * @since 2013-2-11
 */
public class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

	/**
	 * The service-version xml attribute
	 */
	private static final String SERVICE_VERSION_XML_ATTRIBUTE = "service-version";

	/**
	 * The service-group xml attribute
	 */
	private static final String SERVICE_GROUP_XML_ATTRIBUTE = "service-group";

	/**
	 * The client-timeout xml attribute
	 */
	private static final String CLIENT_TIMEOUT_XML_ATTRIBUTE = "client-timeout";

	/**
	 * The client-idle-timeout xml attribute
	 */
	private static final String CLIENT_IDLE_TIMEOUT_XML_ATTRIBUTE = "client-idle-timeout";

	/**
	 * The HSFAnnotationBeanPostProcessor bean name
	 */
	private static final String HSF_ANNOTATION_PROCESSOR_BEAN_NAME = "org.springframework.context.annotation.internalHSFAnnotationProcessor";

	/**
	 * The HSFExportAnnotationBeanFactoryPostProcessor bean name
	 */
	private static final String HSF_EXPORT_ANNOTATION_PROCESSOR_BEAN_NAME = "org.springframework.context.annotation.internalHSFExportAnnotationProcessor";

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {

		Set<BeanDefinitionHolder> processorDefinitions = registerAnnotationConfigProcessors(element, parserContext);

		for (BeanDefinitionHolder processorDefinition : processorDefinitions) {
			parserContext.registerBeanComponent(new BeanComponentDefinition(processorDefinition));
			// parserContext.getReaderContext().fireComponentRegistered(new
			// BeanComponentDefinition(processorDefinition));
		}

		return null;
	}

	/**
	 * Register all relevant annotation post processors in the given registry.
	 * 
	 * @param element the element to operate on
	 * @param parserContext the parserContext to operate on
	 * @return a Set of BeanDefinitionHolders, containing all bean definitions that have actually been registered by
	 *         this call
	 */
	public Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(Element element, ParserContext parserContext) {
		Object source = parserContext.extractSource(element);

		Set<BeanDefinitionHolder> beanDefs = new LinkedHashSet<BeanDefinitionHolder>(2);

		BeanDefinitionRegistry registry = parserContext.getRegistry();

		if (!registry.containsBeanDefinition(HSF_EXPORT_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			BeanDefinitionBuilder builder = createRootBeanDefinition(source,
					HSFExportAnnotationBeanFactoryPostProcessor.class);
			addGlobalPropertyValues(builder, element);
			beanDefs.add(new BeanDefinitionHolder(builder.getBeanDefinition(),
					HSF_EXPORT_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		if (!registry.containsBeanDefinition(HSF_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			BeanDefinitionBuilder builder = createRootBeanDefinition(source, HSFAnnotationBeanPostProcessor.class);
			beanDefs.add(new BeanDefinitionHolder(builder.getBeanDefinition(), HSF_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		return beanDefs;
	}

	/**
	 * Creates a root BeanDefinition.
	 * 
	 * @param source the configuration source element (already extracted) that this registration was triggered from. May
	 *            be <code>null</code>.
	 * @param beanClass the bean class for creating bean definition
	 * @return the beanDefinitionBuilder instance
	 */
	private BeanDefinitionBuilder createRootBeanDefinition(Object source, Class<?> beanClass) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
		builder.getRawBeanDefinition().setSource(source);
		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		return builder;
	}

	/**
	 * Adds global property values to the builder
	 * 
	 * @param builder the builder for add property values to
	 * @param element the element to operate on
	 */
	private void addGlobalPropertyValues(BeanDefinitionBuilder builder, Element element) {
		if (element.hasAttribute(SERVICE_VERSION_XML_ATTRIBUTE)) {
			addPropertyValue(builder, "serviceVersion", element.getAttribute(SERVICE_VERSION_XML_ATTRIBUTE));
		}
		if (element.hasAttribute(SERVICE_GROUP_XML_ATTRIBUTE)) {
			addPropertyValue(builder, "serviceGroup", element.getAttribute(SERVICE_GROUP_XML_ATTRIBUTE));
		}
		if (element.hasAttribute(CLIENT_TIMEOUT_XML_ATTRIBUTE)) {
			addPropertyValue(builder, "clientTimeout", element.getAttribute(CLIENT_TIMEOUT_XML_ATTRIBUTE));
		}
		if (element.hasAttribute(CLIENT_IDLE_TIMEOUT_XML_ATTRIBUTE)) {
			addPropertyValue(builder, "clientIdleTimeout", element.getAttribute(CLIENT_IDLE_TIMEOUT_XML_ATTRIBUTE));
		}
	}
}
