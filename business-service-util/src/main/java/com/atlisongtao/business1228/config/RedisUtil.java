package com.atlisongtao.business1228.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    //创建链接池对象
    private JedisPool jedisPool = null;
    //给JedisPool 做一个初始化方法
    public void initJedisPool(String host, int port, int database){
        //初始化jedisPool 的过程中，可以需要配置初始化的参数
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //设置参数 ，总数
        jedisPoolConfig.setMaxTotal(100);
        //等待时间
        jedisPoolConfig.setMaxWaitMillis(10*1000);

        jedisPoolConfig.setMinIdle(10);

        //设置一下如果达到最大链接数，需要等待的时间
        jedisPoolConfig.setBlockWhenExhausted(true);

        jedisPool = new JedisPool(jedisPoolConfig,host,port,20*1000);
    }

    //获取jedis对象
    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
