package com.atlisongtao.business1228.service;



import com.atlisongtao.business1228.bean.CartInfo;

import java.util.List;

public interface CartService {
    /**
     *
     * @param skuId 商品Id
     * @param userId 用户Id
     * @param skuNum 购买的数量
     */
    void  addToCart(String skuId, String userId, Integer skuNum);

    /**
     *
     * @param userId 用户Id
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 合并购物车
     * @param cartListCK
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId);

    /**
     * 更改商品的选中状态
     * @param skuId
     * @param isChecked
     * @param userId
     */
    void checkCart(String skuId, String isChecked, String userId);

    /**
     * 根据userId 取得用户想要购买那些商品！
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
}
