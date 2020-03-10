package com.atlisongtao.business1228.service;


import com.atlisongtao.business1228.bean.UserAddress;
import com.atlisongtao.business1228.bean.UserInfo;

import java.util.List;

public interface UserInfoService {
    // 查询所有用户数据
    List<UserInfo> findAll();
    // 根据用户Id 查询用户地址
    List<UserAddress> findUserAddressByUserId(String userId);
    // 登录的方法
    UserInfo login(UserInfo userInfo);
    // 根据用户Id验证redis 中是否有数据
    UserInfo verify(String userId);
}
