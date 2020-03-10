package com.atlisongtao.business1228.usermanage.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atlisongtao.business1228.bean.UserAddress;
import com.atlisongtao.business1228.bean.UserInfo;
import com.atlisongtao.business1228.config.RedisUtil;
import com.atlisongtao.business1228.service.UserInfoService;
import com.atlisongtao.business1228.usermanage.mapper.UserAddressMapper;
import com.atlisongtao.business1228.usermanage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    public String USERKEY_PREFIX="user:";
    public String USERINFOKEY_SUFFIX=":info";
    public int USERKEY_TIMEOUT=60*60*24;


    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> findUserAddressByUserId(String userId) {
        //select * from user_address where userId = ?
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);//userId = ? 传入userId
        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        //对用户密码进行加密
        String passwd = userInfo.getPasswd();
        //加密
        String newPassword = DigestUtils.md5DigestAsHex(passwd.getBytes());
        //加密后的密码放入对象中
        userInfo.setPasswd(newPassword);
        //登陆成功后将用户信息放入缓存中
        UserInfo info = userInfoMapper.selectOne(userInfo);
        if (info!=null){
            //加入到Redis
            Jedis jedis = redisUtil.getJedis();
            //过期时间设计
            String userKey = USERKEY_PREFIX+info.getId()+USERINFOKEY_SUFFIX;
            jedis.setex(userKey,USERKEY_TIMEOUT, JSON.toJSONString(info));
            jedis.close();

            //返回的数据
            return info;
        }

        return userInfoMapper.selectOne(userInfo);
    }

    @Override
    public UserInfo verify(String userId) {
        //先获取redis
        Jedis jedis = redisUtil.getJedis();
        //取值， 定义key
        String userKey = USERKEY_PREFIX+userId+USERINFOKEY_SUFFIX;
        //取出key中的数据
        String userJson = jedis.get(userKey);
        //更新用户过期时间
        jedis.expire(userKey,USERKEY_TIMEOUT);
        //将字符串转换为对象
        if (userJson!=null && !"".equals(userJson)){
            UserInfo userInfo = JSON.parseObject(userJson,UserInfo.class);
            return userInfo;
        }
        jedis.close();
        return null;
    }
}
