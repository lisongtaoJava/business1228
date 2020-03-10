package com.atlisongtao.business1228.item.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atlisongtao.business1228.bean.SkuInfo;
import com.atlisongtao.business1228.bean.SkuSaleAttrValue;
import com.atlisongtao.business1228.bean.SpuSaleAttr;
import com.atlisongtao.business1228.config.LoginRequire;
import com.atlisongtao.business1228.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {
    @Reference
    private ManageService manageService;

    //@Reference
    //private ListService listService;


    @RequestMapping("{skuId}.html")
    //@LoginRequire(autoRedirect = true)//访问商品详情的时候，必须登录
    public String skuInfoPage(@PathVariable("skuId") String skuId, HttpServletRequest request){
        System.out.println("skuId:"+skuId);
        // 在后台保存数据，给前台页面渲染
        // 必须通过skuInfo.id ==skuId 去查询数据，自然要调用服务层
        SkuInfo skuInfo =  manageService.getSkuInfo(skuId);

        // 查询销售属性，销售属性值
        List<SpuSaleAttr> saleAttrList =  manageService.selectSpuSaleAttrListCheckBySku(skuInfo);

        // 得到销售属性值，skuId的数据集合
        List<SkuSaleAttrValue> skuSaleAttrValueList =  manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        // 将集合的数据做成json字符串 {"167|169":"45","166|170":"46","166| 169":"44"} sale_attr_value_id skuId
        // 看成Map map.put("jsonkey",skuId)
        // 声明一个key的字符串
        String jsonkey="";

        HashMap<String, Object> map = new HashMap<>();
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
            // 获取到当前集合中的每一个对象
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);
            // 第二次拼接 jsonkey=167|
            if (jsonkey.length()!=0){
                jsonkey+="|";
            }
            // jsonkey=167
            jsonkey+=skuSaleAttrValue.getSaleAttrValueId();
            // 当拼接完成之后，应该将数据添加到map中,什么时候将数据放入map 中
            if ((i+1)==skuSaleAttrValueList.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())){
                map.put(jsonkey,skuSaleAttrValue.getSkuId());
                jsonkey="";
            }
        }
        // 将map 转换为json 字符串
        String valuesSkuJson = JSON.toJSONString(map);
        System.out.println("valuesSkuJson:"+valuesSkuJson);
        // 将字符串json 进行保存
        request.setAttribute("valuesSkuJson",valuesSkuJson);

        // 数据保存

        request.setAttribute("saleAttrList",saleAttrList);

        // 将skuInfo保存到作用域
        request.setAttribute("skuInfo",skuInfo);
        //调用热度排名接口
        //listService.incrHotScore(skuId);

        return "item";
    }
}