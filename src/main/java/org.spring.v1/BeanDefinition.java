package org.spring.v1;

public class BeanDefinition {

    private Class<?> beanClass;

    public Class<?> getBeanClass(){
        return this.beanClass;
    }

    public void setBeanClass(Class<?> beanClass){
        this.beanClass = beanClass;
    }

//    public BeanDefinition(Class<?> beanClass){
//        this.beanClass = beanClass;
//    }

}
