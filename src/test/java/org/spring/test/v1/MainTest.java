package org.spring.test.v1;

import org.spring.test.v1.service.UserRepository;
import org.spring.test.v1.service.UserService;
import org.spring.v1.BeanDefinition;
import org.spring.v1.DefaultListableBeanFactory;

public class MainTest {

    public static void main(String[] args) throws Exception {

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("userService", new BeanDefinition(UserService.class));
        beanFactory.registerBeanDefinition("userRepository", new BeanDefinition(UserRepository.class));

        UserService us1= (UserService) beanFactory.getBean("userService");
//        UserService us2= (UserService) beanFactory.getBean("userService");
        us1.hello();
//        System.out.println(us1 == us2);
    }
}
