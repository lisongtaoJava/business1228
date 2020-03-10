package com.atlisongtao.business1228.service;



import com.atlisongtao.business1228.bean.*;
import java.util.List;

public interface ManageService {

    // 查询所有一级分类
    List<BaseCatalog1> getCatalog1();
    // 根据一级分类查询二级分类
    List<BaseCatalog2> getCatalog2(String catalog1Id);
    // 根据二级分类查询三级分类
    List<BaseCatalog3> getCatalog3(String catalog2Id);
    // 根据三级分类Id查询平台属性列表
    List<BaseAttrInfo> getAttrList(String catalog3Id);
    // 大保存平台属性，平台属性值
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    // 根据平台属性Id 查询平台属性对象
    BaseAttrInfo getAttrInfo(String attrId);

    // 根据三级分类Id查询商品列表 catalog3Id,id,spuName
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    // 查询所有的销售属性列表
    List<BaseSaleAttr> getBaseSaleAttrList();

    // 保存事件
    void saveSpuInfo(SpuInfo spuInfo);

    // 根据spuId查询spuImage
    List<SpuImage> getSpuImageList(String spuId);

    // 根据spuId 查询销售属性列表集合
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    // 保存skuInfo
    void saveSkuInfo(SkuInfo skuInfo);
    // 根据skuId 查询skuInfo 信息
    SkuInfo getSkuInfo(String skuId);
    // 查询销售属性，销售属性值 {skuInfo.id skuInfo.spuId}
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo);
    // 根据spuId 查询skuSaleAttrValue
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
    // 通过平台属性值Id 查询平台属性集合
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
