package com.atlisongtao.business1228.usermanage.controller;


import com.atlisongtao.business1228.bean.UserInfo;
import com.atlisongtao.business1228.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private  UserInfoService userInfoService;

    @RequestMapping("findAll")
    @ResponseBody
    public List<UserInfo> findAll(){
       return userInfoService.findAll();
    }


}
