package org.spring.v1;

import java.lang.reflect.Proxy;

//语义上等价 Spring 的 AbstractAutoProxyCreator
public class SimpleAopPostProcessor implements SmartInstantiationAwareBeanPostProcessor{
    @Override
    public Object getEarlyBeanReference(Object bean, String beanName) {
        return wrapIfNecessary(bean);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return wrapIfNecessary(bean);
    }

    private Object wrapIfNecessary(Object bean){
        //这里还是继续模拟Service就需要代理
        if(!bean.getClass().getSimpleName().endsWith("Service")){
            return bean;
        }

        Class<?>[] interfaces = bean.getClass().getInterfaces();
        if(interfaces.length == 0){
            return bean;
        }
        return Proxy.newProxyInstance(
          bean.getClass().getClassLoader(),
          interfaces,
                (proxy, method, args) -> {
                    System.out.println("[AOP before]" + method.getName());
                    Object result = method.invoke(bean, args);
                    System.out.println("[AOP after]" + method.getName());
                    return result;
                }
        );

    }
}
