package com.atlisongtao.business1228.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.atlisongtao.business1228.bean.BaseSaleAttr;
import com.atlisongtao.business1228.bean.SpuInfo;
import com.atlisongtao.business1228.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SpuManageController {

    @Reference
    private ManageService manageService;
    @RequestMapping("spuListPage")
    public String spuListPage(){
        return "spuListPage";
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> spuList(String catalog3Id){
        // 声明spuInfo 对象
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return manageService.getSpuInfoList(spuInfo);
    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){
      return   manageService.getBaseSaleAttrList();
    }

    // 控制器保存 url 路径从页面！
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){

        // 调用服务层
        manageService.saveSpuInfo(spuInfo);
        return "success";
    }
}
