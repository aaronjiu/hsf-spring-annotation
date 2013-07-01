package com.taobao.hsf.spring.config;

import static com.taobao.hsf.spring.util.BeanDefinitionUtils.addPropertyValue;
import static com.taobao.hsf.spring.util.ClassNameContants.HSF_SPRING_CONSUMER_BEAN_CLASS_NAME;
import static com.taobao.hsf.spring.util.ClassNameContants.INIT_METHOD_NAME;
import static com.taobao.hsf.spring.util.ClassNameContants.METHOD_SPECIAL_CLASS_NAME;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.StringUtils;

import com.taobao.hsf.spring.annotation.HSF;

/**
 * Bean post processor for {@link HSF} annotation
 * 
 * @author <a href="mailto:tonglin@taobao.com">tonglin</a>
 * @version 1.0
 * @since 2013-2-18
 */
public class HSFAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements
		BeanFactoryAware, PriorityOrdered {

	/**
	 * The interfaceName property
	 */
	private static final String INTERFACE_NAME_ATTRIBUTE = "interfaceName";
	/**
	 * The version property
	 */
	private static final String VERSION_ATTRIBUTE = "version";
	/**
	 * The target property
	 */
	private static final String TARGET_ATTRIBUTE = "target";
	/**
	 * The group property
	 */
	private static final String GROUP_ATTRIBUTE = "group";
	/**
	 * The methodSpecials property
	 */
	private static final String METHOD_SPECIALS_ATTRIBUTE = "methodSpecials";
	/**
	 * The clientTimeout property
	 */
	private static final String CLIENT_TIMEOUT_ATTRIBUTE = "clientTimeout";
	/**
	 * The methodName property
	 */
	private static final String METHOD_NAME_ATTRIBUTE = "methodName";

	private transient ConfigurableListableBeanFactory beanFactory;

	private int order = Ordered.LOWEST_PRECEDENCE - 4;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (beanFactory instanceof ConfigurableListableBeanFactory) {
			this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
		}
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		postAnnotation(bean.getClass());
		return true;
	}

	private void postAnnotation(Class<?> beanType) throws BeansException {
		ReflectionUtils.doWithFields(beanType, new FieldCallback() {

			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				HSF annotation = field.getAnnotation(HSF.class);
				if (annotation != null) {
					String beanName = guessBeanName(annotation, field);
					if (!beanFactory.containsBeanDefinition(beanName)) {
						AbstractBeanDefinition beanDefinition = createHSFSpringConsumerBeanDefinition(field,
								annotation, beanFactory);
						registerBeanDefinition((BeanDefinitionRegistry) beanFactory, beanName, beanDefinition);
					}
				}
			}

			/**
			 * @param field
			 * @param annotation
			 * @param beanFactory TODO
			 * @return
			 */
			private AbstractBeanDefinition createHSFSpringConsumerBeanDefinition(Field field, HSF annotation,
					ConfigurableListableBeanFactory beanFactory) {
				BeanDefinitionBuilder builder = BeanDefinitionBuilder
						.genericBeanDefinition(HSF_SPRING_CONSUMER_BEAN_CLASS_NAME);
				builder.setInitMethodName(INIT_METHOD_NAME);
				// 必须配置[String]，调用的服务的接口名称
				addPropertyValue(builder, INTERFACE_NAME_ATTRIBUTE, field.getType().getName());
				// 可选配置[String]，调用的服务的版本，默认为1.0.0
				addPropertyValue(builder, VERSION_ATTRIBUTE, beanFactory.resolveEmbeddedValue(annotation.version()));
				// 可选配置[String]，调用的服务所在的组，默认为HSF
				addPropertyValue(builder, GROUP_ATTRIBUTE, beanFactory.resolveEmbeddedValue(annotation.group()));
				// 可选配置[String]，调用的服务的地址和端口
				addPropertyValue(builder, TARGET_ATTRIBUTE, beanFactory.resolveEmbeddedValue(annotation.target()));
				// 可选配置，含义为为方法单独配置超时(单位ms)，这样接口中的方法可以采用不同的超时时间，
				// 该配置优先级高于服务端的超时配置
				addPropertyValue(builder, METHOD_SPECIALS_ATTRIBUTE, createMethodSpecials(annotation.methodSpecials()));

				return builder.getBeanDefinition();
			}

			/**
			 * @param methodSpecials the method specials array
			 * @return A ManagedList<BeanDefinition>
			 */
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
			 * rigister the bean definition with the rigistry
			 * 
			 * @param registry the regigitry
			 * @param beanName the bean name
			 * @param beanDefinition
			 */
			private void registerBeanDefinition(BeanDefinitionRegistry registry, String beanName,
					AbstractBeanDefinition beanDefinition) {
				registry.registerBeanDefinition(beanName, beanDefinition);
			}

			/**
			 * Guess the bean name
			 * 
			 * @param annotation HSF annotation
			 * @param field the field to get name
			 * @return the bean name
			 */
			private String guessBeanName(HSF annotation, Field field) {
				return StringUtils.hasText(annotation.name()) ? annotation.name() : field.getName();
			}

		});
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return this.order;
	}

}
