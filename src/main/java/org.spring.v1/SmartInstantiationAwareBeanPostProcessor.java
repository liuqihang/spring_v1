package org.spring.v1;

public interface SmartInstantiationAwareBeanPostProcessor extends BeanPostProcessor{

    default Object getEarlyBeanReference(Object bean, String beanName){
        return bean;
    }
}
