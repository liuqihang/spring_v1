package org.spring.v1;

import java.lang.reflect.Field;
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
    public Object getBean(String beanName) {
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

        // 属性填充
        populateBean(bean);

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

    private void populateBean(Object bean){
        Class<?> clazz = bean.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if(field.isAnnotationPresent(Autowired.class)){
                Class<?> dependencyType = field.getType();

                String dependencyName = lowerFirst(dependencyType.getSimpleName());

                Object dependency = getBean(dependencyName);
                try {
                    field.setAccessible(true);
                    field.set(bean, dependency);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private String lowerFirst(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

}
