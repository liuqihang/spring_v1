package org.spring.test.v1;

import org.spring.test.v1.service.IUserService;
import org.spring.test.v1.service.UserRepository;
import org.spring.test.v1.service.UserService;
import org.spring.v1.BeanDefinition;
import org.spring.v1.DefaultListableBeanFactory;

public class MainTest {

    public static void main(String[] args) throws Exception {

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("userService", new BeanDefinition(UserService.class));
        beanFactory.registerBeanDefinition("userRepository", new BeanDefinition(UserRepository.class));

        // 这里糊涂了，绕了半天没理解，忽略了JDK代理必须有实现接口。所以这里返回的是接口类，不然直接强转为UserService会报错的
        IUserService us1= (IUserService)beanFactory.getBean("userService");
        IUserService us2= (IUserService) beanFactory.getBean("userService");
        System.out.println(us1 == us2);
        us1.hello();
//        System.out.println(us1 == us2);
    }
}
