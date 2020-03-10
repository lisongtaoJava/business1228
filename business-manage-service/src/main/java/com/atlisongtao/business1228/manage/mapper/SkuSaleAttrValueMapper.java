package com.atlisongtao.business1228.manage.mapper;


import com.atlisongtao.business1228.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {

    // 根据spuId 查询属性值列表集合
    List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);
}
