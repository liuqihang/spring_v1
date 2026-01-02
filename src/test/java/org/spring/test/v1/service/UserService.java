package org.spring.test.v1.service;

import org.spring.v1.Autowired;

public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void hello(){
        userRepository.selectById();
        System.out.println("hello, my spring first query");

    }
}
