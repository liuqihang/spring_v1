package org.spring.v1;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultListableBeanFactory implements BeanFactory{

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>();

    //一级缓存( 解决singleton的问题, 避免getBean同一个对象，堆地址却不同)
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    //二级缓存( 设置一个”提前暴露区“)
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();

    //三级缓存( 提供早期对象bean引用的能力)
    private final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>();

    //存储早期曝光的bean集合
    private final Set<String> earlyExposedBeans =ConcurrentHashMap.newKeySet();

    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    //这里有一个职责的区分：getBean只负责读缓存
    @Override
    public Object getBean(String beanName) {
        Object singleton = getSingleton(beanName);
        if(singleton != null){
            return singleton;
        }

        return createBean(beanName);
    }

    private Object getSingleton(String beanName){
        Object singleton = singletonObjects.get(beanName);
        if(singleton == null){
            Object earlySingleton = earlySingletonObjects.get(beanName);
            if(earlySingleton == null){
                ObjectFactory<?> objectFactory = singletonFactories.get(beanName);
                if(objectFactory != null){
                    Object earlyReference = objectFactory.getObject();
                    earlySingletonObjects.put(beanName, earlyReference);
                    singletonFactories.remove(beanName);

                    earlyExposedBeans.add(beanName);
                    return earlyReference;
                }else {
                    return null;
                }
            }
            return earlySingleton;
        }else {
           return singleton;
        }
    }

    //职责区分：getBean负责读load, 那createBean就负责写缓存store
    private Object createBean(String beanName){
        BeanDefinition db = beanDefinitionMap.get(beanName);
        if(db == null){
            throw new RuntimeException("No bean named '" + beanName + "' is defined");
        }
        // 实例化
        Object bean = instantiateBean(db);

        //放入三级缓存
        singletonFactories.put(beanName, ()->getEarlyBeanReference(beanName, bean));

        // 属性填充
        populateBean(bean);

        Object exposeObject = bean;

        if(earlyExposedBeans.contains(beanName)){
            exposeObject = earlySingletonObjects.get(beanName);
        }else {
            exposeObject = initializeBean(bean);
        }

        // 放入一级缓存,移除二级、三级缓存
        singletonObjects.put(beanName, exposeObject);
        earlySingletonObjects.remove(beanName);
        singletonFactories.remove(beanName);
        return exposeObject;
    }

    private Object instantiateBean(BeanDefinition beanDefinition){
        try {
            return beanDefinition.getBeanClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 设计动机：
     *      populateBean 的核心职责，是在属性填充阶段通过 getBean 建立 Bean 之间的依赖关系，从而构建完整的依赖图。
     *      而一旦属性注入依赖 getBean，就不可避免地会触发循环依赖问题，因此 Spring 必须在实例化完成后、属性填充之前将 Bean 的“对象引用”提前暴露到 earlySingletonObjects，以保证依赖图可以被正确闭合。
     *
     *      现阶段：populateBean应该是Spring 中唯一一个 “主动触发依赖查找” 的阶段
     *
     *      到这里应该可以解释为什么循环依赖只发生在字段的setter注入，而构造器注入天然的解决不了循环依赖
     *      这里我使用伪代码描述为什么构造器注入天然解决不了循环依赖：
     *      class A {
     *          private final B b;
     *          public A(B b){
     *              ...
     *          }
     *      }
     *
     *      class B{
     *          private final A a;
     *          public B(A a){
     *              ...
     *          }
     *      }
     *      step 1: 需要new A的时候，得先拿到B
     *      step 2：于是去new B, 这个时候需要拿到A
     *      step 3：于是再去new A，发现还是得拿到B
     *      死循环了...，构造器注入在实例化阶段就需要完整的依赖，因此不具备提前暴露的可能性
     *
     *      而setter注入解决循环依赖的过程是：
     *      class A {
     *          @Autowired
     *          B b;
     *      }
     *      执行顺序：
     *      step 1：new A
     *      step 2:填充属性 getBean(B)
     *      step 3: A.b = getBean(B)
     *      new就已经完成了，只是补字段（半成品）。
     *
     *      ！！！但相信很多同学实际工作中使用过构造器注入，我本人也使用过，Spring官方也更推荐使用构造器注入，因为不可变、依赖明确，但代价就是你自己得保证没有循环依赖，这不是确定，是对代码结构设计的一种强约束性。
     *
     * @param bean
     */
    private void populateBean(Object bean){
        Class<?> clazz = bean.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if(field.isAnnotationPresent(Autowired.class)){
                Class<?> dependencyType = field.getType();

//                String dependencyName = lowerFirst(dependencyType.getSimpleName());

                //populateBean方法内部调用getBean()是Spring 能形成 “依赖图/依赖关系” 的根本原因
//                Object dependency = getBean(dependencyName);
                Object dependency = getBeanByType(dependencyType);
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

    // AOP关键扩展点
    private Object getEarlyBeanReference(String name, Object bean){
        //这里就模拟名称以Service结尾的就需要代理吧
        if(bean.getClass().getSimpleName().endsWith("Service")){
            return Proxy.newProxyInstance(
                    bean.getClass().getClassLoader(),
                    bean.getClass().getInterfaces(),
                    (proxy, method, args) -> {
                        System.out.println("[AOP before]" + method.getName());
                        Object result = method.invoke(bean, args);
                        System.out.println("[AOP after]" + method.getName());
                        return result;
                    }
            );
        }
        return bean;
    }

    //初始化bean
    private Object initializeBean(Object bean){
        return bean;
    }

    private Object getBeanByType(Class<?> type){
        Object candidate = null;
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            Class<?> beanClass = entry.getValue().getBeanClass();

            if (type.isAssignableFrom(beanClass)) {
                if (candidate != null) {
                    throw new RuntimeException(
                            "Multiple beans found for type " + type.getName()
                    );
                }
                candidate = getBean(entry.getKey());
            }
        }

        if (candidate == null) {
            throw new RuntimeException(
                    "No bean found for type " + type.getName()
            );
        }

        return candidate;
    }

}
