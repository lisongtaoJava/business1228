package com.atlisongtao.business1228.manage.mapper;


import com.atlisongtao.business1228.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    // 根据spuId 查询数据 List<SpuSaleAttr>
    List<SpuSaleAttr> selectSpuSaleAttrList(String spuId);
    // 根据skuId ，spuId 查询销售属性列表
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(long skuId, long spuId);
}
