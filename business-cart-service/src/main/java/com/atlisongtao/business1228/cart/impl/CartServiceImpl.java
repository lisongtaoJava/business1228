package com.atlisongtao.business1228.cart.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;

import com.atlisongtao.business1228.bean.CartInfo;
import com.atlisongtao.business1228.bean.SkuInfo;
import com.atlisongtao.business1228.cart.constant.CartConst;
import com.atlisongtao.business1228.cart.mapper.CartInfoMapper;
import com.atlisongtao.business1228.config.RedisUtil;
import com.atlisongtao.business1228.service.CartService;
import com.atlisongtao.business1228.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    /**
     * @param skuId  商品Id
     * @param userId 用户Id
     * @param skuNum 购买的数量
     */
    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Autowired
    private RedisUtil redisUtil;
    
    @Reference
    private ManageService manageService;

    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        // 添加购物车的业务
        // 查看购物车是否有该商品cartInfo
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        // 根据userId and skuId 查询 45,46 {44}
        CartInfo cartInfoExist  = cartInfoMapper.selectOne(cartInfo);
        if (cartInfoExist!=null){
            // 该商品存在，修改数量
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            // 给实时价格赋值
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            // 更数据库
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
            // 放入缓存！
        }else {
            // 新增商品 商品的数据来源，来自skuInfo。
            // 得到skuInfo ，根据skuId 查询skuInfo 信息
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuNum(skuNum);
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuId(skuId);
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setCartPrice(skuInfo.getPrice());
            // 添加数据库
            cartInfoMapper.insertSelective(cartInfo1);
            //. 放入redsi
            cartInfoExist=cartInfo1;
        }

        // 提出公共的对象，放入redsi
        Jedis jedis = redisUtil.getJedis();
        // 定义 cart key= user:userId:cart             用户 user:userId:info
        String cartKey= CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        // value 里面放入的应该是cartInfo ，{即表示修改，也表示新增}
        jedis.hset(cartKey,skuId, JSON.toJSONString(cartInfoExist));
        // 买东西user ,user处于活跃状态！得到用户key
        String userKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        // 给cartKey设置一下过期时间 给用户key的过期时间
        Long ttl = jedis.ttl(userKey);
        jedis.expire(cartKey,ttl.intValue());
        jedis.close();

    }

    /**
     * @param userId 用户Id
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId) {
        //
        List<CartInfo> cartInfoList = new ArrayList<>();
        // 从redis 取得数据
        Jedis jedis = redisUtil.getJedis();
        // 定义key user:userId:cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        // 根据key 取得购物车数据
        List<String> cartJsons  = jedis.hvals(userCartKey);
        if (cartJsons!=null && cartJsons.size()>0){
            // 循环遍历
            for (String cartJson : cartJsons) {
                // cartJson 就一个cartInfo 对象
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            // cartInfoList 对其进行排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    //  comparable  compareTo
                    // 此处 compareTo 是String 类方法
                    // 字符串长度 length(); 数组长度 length; 集合 size(); 文件：length();
                    return o1.getId().compareTo(o2.getId());
                }
            });
            return cartInfoList;
        }else {
            // redis 没有数据库，从数据库取得数据
            List<CartInfo> cartInfos = loadCartCache(userId);
            return cartInfos;
        }
    }

    /**
     * 合并购物车
     *
     * @param cartListCK
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId) {
        // 能够得到最新的商品价格的cartInfoList
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        // CK ,redis 判断条件 是否有相同的skuId
        // 将cookie 中的数据都添加到数据库！ 购物车列表展示时，数量的合并！
        for (CartInfo cartInfoCK : cartListCK) {
            boolean isMatch =false;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                // 匹配上
                if (cartInfoCK.getSkuId().equals(cartInfoDB.getSkuId())){
                    // 数量相加
                    cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfoCK.getSkuNum());
                    // 更新数据库
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch = true;
                }
            }
            // 没有匹配上！
            if (!isMatch){
                // 直接插入数据库！将userId 存入到数据库
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }
        // 放入redis
        List<CartInfo> cartInfoList = loadCartCache(userId);
        // 合并勾选状态数据 CK,DB ,合并条件 skuId，isChecked=1
        // 双重循环来判断
        for (CartInfo cartInfoDB : cartInfoList) {
            for (CartInfo cartInfoCK : cartListCK) {
                // skuId 相同
                if (cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())){
                    if ("1".equals(cartInfoCK.getIsChecked())){
                        // 被选中状态之后的数据合并
                        //  判断，当前登录状态的购物车，与cookie 中的购物车同时是选中状态 做add，如果单方：数量只做单方！
                        // cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfoCK.getSkuNum());
                        cartInfoDB.setIsChecked(cartInfoCK.getIsChecked());
                        // 保存勾选状态到redis
                        checkCart(cartInfoDB.getSkuId(),cartInfoCK.getIsChecked(),userId);
                    }
                }
            }
        }
        // 返回最终的合并数据！
        return cartInfoList;
    }

    /**
     * 更改商品的选中状态
     *
     * @param skuId
     * @param isChecked
     * @param userId
     */
    @Override
    public void checkCart(String skuId, String isChecked, String userId) {

        // 获取Jedis
        Jedis jedis = redisUtil.getJedis();
        // 定义获取数据的购物车key user:userId:cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        // 获取key 对应的值 hvals hget
        String cartJson  = jedis.hget(userCartKey, skuId);
        // 将字符串转化成对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        // 给cartInfo 赋值 {isChecked}
        cartInfo.setIsChecked(isChecked);
        // 将赋值好的对象 再放入redis
        jedis.hset(userCartKey,skuId,JSON.toJSONString(cartInfo));
        // 新增一个key 来专门存储被选中的商品
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        if ("1".equals(isChecked)){
            // user:userId:checked 保存被选中的商品
            jedis.hset(userCheckedKey,skuId,JSON.toJSONString(cartInfo));
        }else {
            // 将key 删除
            jedis.hdel(userCheckedKey,skuId);
        }
        jedis.close();
    }

    /**
     * 根据userId 取得用户想要购买那些商品！
     *
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        // 定义一个集合
        List<CartInfo> cartInfoList = new ArrayList<>();
        // 哪些商品被选中，redis 中 user:2:checked
        // 获取jedis 对象
        Jedis jedis = redisUtil.getJedis();
        // 定义key
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        // 获取到所有数据

        List<String> stringList = jedis.hvals(userCheckedKey);
        if (stringList!=null && stringList.size()>0){
            for (String cartInfoStr : stringList) {
                // 将cartInfo 转换
                CartInfo cartInfo = JSON.parseObject(cartInfoStr, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
        }
        jedis.close();
        return cartInfoList;
    }

    //
    private List<CartInfo> loadCartCache(String userId) {
        // cartInfo -- 购物车价格，实时价格{skuInfo.price}
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList==null || cartInfoList.size()==0){
            return null;
        }
        // cartInfoList 放入 redis ！
        Jedis jedis = redisUtil.getJedis();
        // 取得key
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        // 声明一个map
        HashMap<String, String> map = new HashMap<>();
        // 将集合所有数据放入redis
        for (CartInfo cartInfo : cartInfoList) {
            // map.put(field,value)  field = skuId
            map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        jedis.hmset(userCartKey,map);
        jedis.close();
        return cartInfoList;
    }
}
