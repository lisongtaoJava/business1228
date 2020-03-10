package com.atlisongtao.business1228.service;


import com.atlisongtao.business1228.bean.SkuLsInfo;
import com.atlisongtao.business1228.bean.SkuLsParams;
import com.atlisongtao.business1228.bean.SkuLsResult;

public interface ListService {
    void saveSkuInfo(SkuLsInfo skuLsInfo);
    // 通过用户选中的筛选条件来查询数据
    SkuLsResult search(SkuLsParams skuLsParams);
    // 根据skuId 来更新商品的热度排名
    void incrHotScore(String skuId);

}
