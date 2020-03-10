package com.atlisongtao.business1228.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.atlisongtao.business1228.bean.CartInfo;
import com.atlisongtao.business1228.bean.SkuInfo;
import com.atlisongtao.business1228.config.LoginRequire;
import com.atlisongtao.business1228.service.CartService;
import com.atlisongtao.business1228.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private CartCookieHandler cartCookieHandler;
    
    @Reference
    private ManageService manageService;

    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        /*取得到购买的数量*/
        String skuNum = request.getParameter("skuNum");
        // 获取skuId
        String skuId = request.getParameter("skuId");
        // 获取userId ？
        String userId = (String) request.getAttribute("userId");

        // 判断userId
        if (userId!=null){
            // 登录
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else {
            // 未登录 将数据放入cookie 中
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        // 将skuInfo 存储上
        request.setAttribute("skuInfo",skuInfo);

        // 存储购买的数量
        request.setAttribute("skuNum",skuNum);

        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){
        // 取得userId
        String userId = (String) request.getAttribute("userId");
        if (userId!=null){
            List<CartInfo> cartInfoList = null;
            // cookie 取得cookie 中的数据
            List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);
            if (cartListCK!=null && cartListCK.size()>0){
                // 进行合并 cartListCK,userId
                cartInfoList = cartService.mergeToCartList(cartListCK,userId);
                // 合并完成之后，将cookie 的数据是删除！
                cartCookieHandler.deleteCartCookie(request,response);
            }else {
                // 去缓存
                cartInfoList = cartService.getCartList(userId);
            }
           request.setAttribute("cartInfoList",cartInfoList);
        }else {
            // 去cookie 中取得数据
            List<CartInfo> cartInfoList =   cartCookieHandler.getCartList(request);
            request.setAttribute("cartInfoList",cartInfoList);
        }
        return "cartList";
    }

    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        // 获取前天传递过来的参数
        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");
        // 获取userId （判断登录状态）
        String userId = (String) request.getAttribute("userId");

        // 判断当前用户是否登录
        if (userId!=null){
            // 修改redis
            cartService.checkCart(skuId,isChecked,userId);
        }else {
            // 修改cookie
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }

    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request,HttpServletResponse response){
        // 对商品的勾选状态进行合并
        // 获取cookie中的购物车列表
        List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);
        // 合并过程中需要userId
        String userId = (String) request.getAttribute("userId");
        if (cartListCK!=null&& cartListCK.size()>0){
            // 合并勾选状态
            cartService.mergeToCartList(cartListCK,userId);
            // 将cookie 中的数据进行删除
            cartCookieHandler.deleteCartCookie(request,response);
        }
        // 返回订单去结算控制器
        return "redirect://order.gmall.com/trade";
    }




}
