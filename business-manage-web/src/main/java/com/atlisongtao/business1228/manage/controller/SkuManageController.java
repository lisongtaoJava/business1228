package com.atlisongtao.business1228.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.atlisongtao.business1228.bean.SkuInfo;
import com.atlisongtao.business1228.bean.SkuLsInfo;
import com.atlisongtao.business1228.bean.SpuImage;
import com.atlisongtao.business1228.bean.SpuSaleAttr;
import com.atlisongtao.business1228.service.ListService;
import com.atlisongtao.business1228.service.ManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Controller
public class SkuManageController {

    @Reference
    private ManageService manageService;


    @Reference
    private ListService listService;

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId){
        return manageService.getSpuImageList(spuId);
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){
       return   manageService.getSpuSaleAttrList(spuId);

    }

    @RequestMapping("saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return "success";
    }

    @RequestMapping("onSale")
    @ResponseBody
    public void onSale(String skuId){
        // 根据skuId 查询skuInfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        // skuLsInfo 所有数据来源于skuInfo ，必须先查询到skuInfo
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        // 开始给skuLsInfo赋值
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        // org.springframework.beans.BeanUtils.copyProperties(skuInfo,skuLsInfo);
        // 调用 方法
        listService.saveSkuInfo(skuLsInfo);
    }
}
