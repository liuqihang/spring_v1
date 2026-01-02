package org.spring.v1;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultListableBeanFactory implements BeanFactory{

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>();

    //一级缓存
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    @Override
    public Object getBean(String beanName) throws Exception {
        Object singleton = singletonObjects.get(beanName);
        if(singleton != null){
            return singleton;
        }

        return createBean(beanName);
    }

    private Object createBean(String beanName){
        BeanDefinition db = beanDefinitionMap.get(beanName);
        if(db == null){
            throw new RuntimeException("No bean named '" + beanName + "' is defined");
        }
        // 实例化
        Object bean = instantiateBean(db);

        // 放入单例缓存
        singletonObjects.put(beanName, bean);
        return bean;
    }

    private Object instantiateBean(BeanDefinition beanDefinition){
        try {
            return beanDefinition.getBeanClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
