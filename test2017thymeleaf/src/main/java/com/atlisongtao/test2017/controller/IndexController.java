package com.atlisongtao.test2017.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Map;

@Controller
public class IndexController {
    @RequestMapping("index")
    public String testIndex(Map map, Model model, ModelAndView modelAndView, HttpServletRequest request, HttpSession session){

        request.setAttribute("name","手机");

        //存储集合
        ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.add("小米cc9");
        arrayList.add("小米Readmi7");
        arrayList.add("CC9Pro");
        arrayList.add("小米MIX Alpha");

        request.setAttribute("arrayList",arrayList);

        //判断价格是否满足购买条件
        request.setAttribute("price",1298);

        //
        request.setAttribute("stock","库存");
        //test session
        session.setAttribute("address","深圳市龙岗区");

        request.setAttribute("lvse","<span style='color:green'>绿色</span>");

        return "index";
    }

}
