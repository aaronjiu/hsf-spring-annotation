package com.taobao.hsf.spring.config;

import static com.taobao.hsf.spring.util.BeanDefinitionUtils.addPropertyReference;
import static com.taobao.hsf.spring.util.BeanDefinitionUtils.addPropertyValue;
import static com.taobao.hsf.spring.util.ClassNameContants.HSF_SPRING_PROVIDER_BEAN_CLASS_NAME;
import static com.taobao.hsf.spring.util.ClassNameContants.INIT_METHOD_NAME;
import static com.taobao.hsf.spring.util.ClassNameContants.METHOD_SPECIAL_CLASS_NAME;

import java.io.Serializable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.taobao.hsf.spring.annotation.HSFExport;

/**
 * Bean factory post processor for {@link HSFExport} annotation
 * 
 * @author <a href="mailto:tonglin@taobao.com">tonglin</a>
 * @version 1.0
 * @since 2013-2-19
 */
public class HSFExportAnnotationBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	/**
	 * 
	 */
	private static final String TARGET = "target";
	/**
	 * 
	 */
	private static final String SERVICE_INTERFACE_ATTRIBUTE = "serviceInterface";
	/**
	 * 
	 */
	private static final String SERVICE_VERSION_ATTRIBUTE = "serviceVersion";
	/**
	 * 
	 */
	private static final String SERIALIZE_TYPE_ATTRIBUTE = "serializeType";
	/**
	 * 
	 */
	private static final String SERVICE_NAME_ATTRIBUTE = "serviceName";
	/**
	 * 
	 */
	private static final String SERVICE_DESC_ATTRIBUTE = "serviceDesc";
	/**
	 * 
	 */
	private static final String SERVICE_GROUP_ATTRIBUTE = "serviceGroup";
	/**
	 * 
	 */
	private static final String SUPPORT_ASYN_CALL_ATTRIBUTE = "supportAsynCall";
	/**
	 * 
	 */
	private static final String CLIENT_TIMEOUT_ATTRIBUTE = "clientTimeout";
	/**
	 * 
	 */
	private static final String CLIENT_IDLE_TIMEOUT_ATTRIBUTE = "clientIdleTimeout";
	/**
	 * 
	 */
	private static final String METHOD_SPECIALS_ATTRIBUTE = "methodSpecials";
	/**
	 * 
	 */
	private static final String METHOD_NAME_ATTRIBUTE = "methodName";
	/**
	 * 
	 */
	private static final String METHOD_TO_INJECT_CONSUMER_IP_ATTRIBUTE = "methodToInjectConsumerIp";
	/**
	 * The bean name generator
	 */
	private BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

	/**
	 * The global serviceVersion, might to be overriden by the {@link HSFExport} annotation serviceVersion() attribute
	 */
	private String serviceVersion;
	/**
	 * The global serviceGroup, might to be overriden by the {@link HSFExport} annotation serviceGroup() attribute
	 */
	private String serviceGroup;
	/**
	 * The global clientTimeout, might to be overriden by the {@link HSFExport} annotation clientTimeout() attribute
	 */
	private int clientTimeout = -1;
	/**
	 * The global clientIdleTimeout, might to be overriden by the {@link HSFExport} annotation clientIdleTimeout()
	 * attribute
	 */
	private int clientIdleTimeout = -1;

	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public void setServiceGroup(String serviceGroup) {
		this.serviceGroup = serviceGroup;
	}

	public void setClientTimeout(int clientTimeout) {
		this.clientTimeout = clientTimeout;
	}

	public void setClientIdleTimeout(int clientIdleTimeout) {
		this.clientIdleTimeout = clientIdleTimeout;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (String beanName : beanNames) {

			Class<?> clazz = beanFactory.getType(beanName);
			if (null != clazz) {
				HSFExport annotation = AnnotationUtils.findAnnotation(clazz, HSFExport.class);
				if (null != annotation) {
					AbstractBeanDefinition beanDefinition = createBeanDefinition(beanName, clazz, annotation,
							beanFactory);
					beanDefinition.setAutowireCandidate(false);

					BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
					String generateBeanName = generateBeanName(beanDefinition, registry);
					registry.registerBeanDefinition(generateBeanName, beanDefinition);
				}
			}
		}
	}

	/**
	 * @param beanName
	 * @param clazz
	 * @param annotation
	 * @param beanFactory TODO
	 * @return
	 */
	private AbstractBeanDefinition createBeanDefinition(String beanName, Class<?> clazz, HSFExport annotation,
			ConfigurableListableBeanFactory beanFactory) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.genericBeanDefinition(HSF_SPRING_PROVIDER_BEAN_CLASS_NAME);
		builder.setInitMethodName(INIT_METHOD_NAME);

		// 必须配置[String]，为服务对外提供的接口
		String serviceInterface = guessServiceInterface(annotation, clazz);
		addPropertyValue(builder, SERVICE_INTERFACE_ATTRIBUTE, serviceInterface);

		// 必须配置[ref]，为需要发布为HSF服务的spring bean id
		String target = guessTarget(annotation, beanName);
		addPropertyReference(builder, TARGET, target);

		// 可选配置[String]，含义为服务的版本，默认为1.0.0
		addPropertyValue(builder, SERVICE_VERSION_ATTRIBUTE,
				beanFactory.resolveEmbeddedValue(annotation.serviceVersion()), serviceVersion);

		// serviceName为推荐配置[String]，含义为服务的名称，便于管理，默认为null
		addPropertyValue(builder, SERVICE_NAME_ATTRIBUTE, annotation.serviceName());

		// serviceDesc为可选配置[String]，含义为服务的描述信息，便于管理，默认为null
		addPropertyValue(builder, SERVICE_DESC_ATTRIBUTE, annotation.serviceDesc());

		// serviceGroup为可选配置[String]，含义为服务所属的组别，以便按组别来管理服务的配置，默认为HSF
		addPropertyValue(builder, SERVICE_GROUP_ATTRIBUTE, beanFactory.resolveEmbeddedValue(annotation.serviceGroup()),
				serviceGroup);

		// supportAsynCall为可选配置[true|false]，含义为标识此服务是否支持异步调用，默认值为false，也就是不支持异步调用
		addPropertyValue(builder, SUPPORT_ASYN_CALL_ATTRIBUTE, annotation.supportAsynCall());

		// clientTimeout为可选配置[int]，含义为客户端调用此服务时的超时时间，单位为ms，默认为3000ms
		// 该配置对接口中的所有方法生效，但是如果客户端通过MethodSpecial属性对某方法配置了超时时间，则该方法的超时时间以客户端配置为准，其他方法不受影响，还是以服务端配置为准
		addPropertyValue(builder, CLIENT_TIMEOUT_ATTRIBUTE, annotation.clientTimeout(), clientTimeout);

		// clientIdleTimeout为可选配置[int]，含义为客户端连接空闲的超时时间，单位为s，默认为60
		addPropertyValue(builder, CLIENT_IDLE_TIMEOUT_ATTRIBUTE, annotation.clientIdleTimeout(), clientIdleTimeout);

		// serializeType
		addPropertyValue(builder, SERIALIZE_TYPE_ATTRIBUTE, annotation.serializeType().toString());

		// 为可选配置，用于为方法单独配置超时(单位ms)，这样接口中的方法可以采用不同的超时时间，
		// 该配置优先级高于上面的clientTimeout的超时配置，低于客户端的methodSpecials配置
		addPropertyValue(builder, METHOD_SPECIALS_ATTRIBUTE, createMethodSpecials(annotation.methodSpecials()));

		// methodToInjectConsumerIp为可选配置，含义为注入调用端IP的方法，这样业务服务也可以得知是哪个IP在调用，该方法的参数必须为String,所存的变量为threadlocal
		addPropertyValue(builder, METHOD_TO_INJECT_CONSUMER_IP_ATTRIBUTE, annotation.methodToInjectConsumerIp());

		return builder.getBeanDefinition();
	}

	/**
	 * @param split
	 * @return
	 */
	private AbstractBeanDefinition createMethodSpecialBeanDefinition(String[] split) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(METHOD_SPECIAL_CLASS_NAME);
		addPropertyValue(builder, METHOD_NAME_ATTRIBUTE, split[0]);
		addPropertyValue(builder, CLIENT_TIMEOUT_ATTRIBUTE, split[1]);
		return builder.getBeanDefinition();
	}

	/**
	 * @param clazz TODO
	 * @return
	 */
	private String guessServiceInterface(HSFExport annotation, Class<?> clazz) {
		Class<?> serviceInterface = annotation.serviceInterface();
		if (!Serializable.class.equals(serviceInterface)) {
			return serviceInterface.toString().substring("interface ".length());
		} else {
			Class<?>[] interfaces = clazz.getInterfaces();
			if (interfaces.length == 1) {
				return interfaces[0].getName();
			} else {
				throw new IllegalArgumentException("The class " + clazz.getName()
						+ " must set @HSFExport's property serviceInterface or must implements one single interface!");
			}
		}
	}

	private ManagedList<BeanDefinition> createMethodSpecials(String[] methodSpecials) {
		ManagedList<BeanDefinition> list = new ManagedList<BeanDefinition>();

		if (!ObjectUtils.isEmpty(methodSpecials)) {
			for (String method : methodSpecials) {
				String[] method2Timeout = StringUtils.split(method, ":");
				if (!ObjectUtils.isEmpty(method2Timeout) && method2Timeout.length == 2) {
					list.add(createMethodSpecialBeanDefinition(method2Timeout));
				}
			}
		}
		return list;
	}

	/**
	 * Guess the reference bean name for the target attribute
	 * 
	 * @param annotation the {@link HSFExport} annotation
	 * @param beanName the bean name to reference
	 * @return the guessed target reference bean name
	 */
	private String guessTarget(HSFExport annotation, String beanName) {
		String target = annotation.target();
		String value = annotation.value();
		return StringUtils.hasText(target) ? target : StringUtils.hasText(value) ? value : beanName;
	}

	/**
	 * @param definition
	 * @param registry
	 * @return
	 */
	private String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		return beanNameGenerator.generateBeanName(definition, registry);
	}
}
