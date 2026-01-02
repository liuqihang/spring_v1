package org.spring.test.v1;

import org.spring.test.v1.service.UserService;
import org.spring.v1.BeanDefinition;
import org.spring.v1.DefaultListableBeanFactory;

public class MainTest {

    public static void main(String[] args) throws Exception {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClass(UserService.class);

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("userService", beanDefinition);

        UserService us= (UserService) beanFactory.getBean("userService");
        us.hello();
    }
}
