package com.atlisongtao.business1228.manage.controller;



import com.alibaba.dubbo.config.annotation.Reference;
import com.atlisongtao.business1228.bean.*;
import com.atlisongtao.business1228.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
public class ManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("index")
    public String index(){
        // 表示返回的试图名称
        return "index";

    }

    @RequestMapping("attrListPage")
    public String attrListPage(){
        // spring boot 默认与thymeleaf 天然整合
        return "attrListPage";
    }
    // 所有easuyUI要的json数据
    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
        return manageService.getCatalog1();
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        return manageService.getCatalog2(catalog1Id);
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return manageService.getCatalog3(catalog2Id);
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return manageService.getAttrList(catalog3Id);
    }

/*
    class Student{
        int id;
        String name;
    }
    jsp
    <form method="post" actiion="saveAttrInfo">
    <input type="text" name="name">
    <input tyep="submit" value="提交">
    </form>
 */
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return "success";
    }

    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId){
        //  List<BaseAttrValue> 实际上是根据谁 BaseAttrInfo ，attrId=BaseAttrInfo.id;
        // select * from baseAttrValue where attrId = attrId 这样做是不可取的！
        // 通过 attrId 去查询 BaseAttrInfo ，返回 baseAttrInfo.attrValueList
        BaseAttrInfo baseAttrInfo =  manageService.getAttrInfo(attrId);
        return baseAttrInfo.getAttrValueList();

    }



}
