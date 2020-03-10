package com.atlisongtao.business1228.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//使用Configuration来代替xml配置
@Configuration
public class RedisConfig {
    //在类中 获取host，port， database，放入配置文件中
    @Value("${spring.redis.host:disabled}")// disabled 表示一个默认值
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;


    //将RedisUtil注入到容器中
    @Bean
    public RedisUtil getRedisUtil(){
        //判断
        if ("disabled".equals(host)) {
            return null;
        }
        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initJedisPool(host,port,database);
        return redisUtil;
    }
}
