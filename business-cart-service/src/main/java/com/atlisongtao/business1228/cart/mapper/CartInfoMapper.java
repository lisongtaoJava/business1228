package com.atlisongtao.business1228.cart.mapper;


import com.atlisongtao.business1228.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {
    // 根据userId 查询商品的实时价格
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
