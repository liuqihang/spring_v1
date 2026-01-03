package org.spring.test.v1.service;

import org.spring.v1.Autowired;

public class UserRepository {

    @Autowired
    private IUserService userService;

    public void selectById(){
        System.out.println("select 1 from user where id = 1");
    }
}
