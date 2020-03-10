package com.atlisongtao.business1228.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;

import com.atlisongtao.business1228.bean.CartInfo;
import com.atlisongtao.business1228.bean.OrderDetail;
import com.atlisongtao.business1228.bean.OrderInfo;
import com.atlisongtao.business1228.bean.UserAddress;
import com.atlisongtao.business1228.config.LoginRequire;
import com.atlisongtao.business1228.service.CartService;
import com.atlisongtao.business1228.service.OrderService;
import com.atlisongtao.business1228.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    // 调用方法
//    @Autowired
    @Reference
    private UserInfoService userInfoService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;
    // trade?userId=1
//    @RequestMapping("trade")
//    @ResponseBody
//    public List<UserAddress> trade(String userId){
//        return userInfoService.findUserAddressByUserId(userId);
//    }

    @RequestMapping("trade")
    @LoginRequire(autoRedirect = true)
    public String trade(HttpServletRequest request){
        // 获取userId
        String userId = (String) request.getAttribute("userId");
        // 获取收获人的地址信息
        List<UserAddress> userAddressList = userInfoService.findUserAddressByUserId(userId);
        // 送货清单 【来自cartInfoList】 订单明细，订单表
        // 根据userId 查询当前的cartInfoList，将cartInfoList 赋给订单明细
        List<CartInfo> cartInfoList = cartService.getCartCheckedList(userId);


        // 创建一个集合来存储OrderDeailList
        ArrayList<OrderDetail> orderDetailArrayList = new ArrayList<>();
        // 循环遍历cartInfoList
        for (CartInfo cartInfo : cartInfoList) {
            // 创建订单对象
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            // 下订单给购物车的价格
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
//            orderDetail.setOrderId(orderInfo.getId());
            // 将orderDetail 添加到集合
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetailArrayList.add(orderDetail);
        }
        // 创建一个OrderInfo 对象
        OrderInfo orderInfo = new OrderInfo();
        // 存储订单明细集合
        request.setAttribute("orderDetailArrayList",orderDetailArrayList);
        // 把订单明细集合放入订单中
        orderInfo.setOrderDetailList(orderDetailArrayList);
        // 调用计算方法
        orderInfo.sumTotalAmount();
        // 存储总金额
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        // 页面保存数据
        request.setAttribute("userAddressList",userAddressList);
        // 调用生成code方法
        String tradeNo = orderService.getTradeNo(userId);
        // 保存到作用域
        request.setAttribute("tradeNo",tradeNo);
        return "trade";
    }

    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        // 用户Id
        String userId = (String) request.getAttribute("userId");
        // 获取tradeNo
        String tradeNo = request.getParameter("tradeNo");

        // 调用服务层的比较code方法
        boolean result = orderService.checkTradeCode(userId, tradeNo);
        if (!result){
            // 存储错误信息
            request.setAttribute("errMsg","友情提示，该页面已失效，请重新刷新！");
            // 返回错误页面
            return "tradeFail";
        }
        // 验证库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            // 调用库存接口
            boolean flg = orderService.checkStock(orderDetail.getSkuId(),orderDetail.getSkuNum());
            // 库存不足
            if (!flg){
                // 存储错误信息
                request.setAttribute("errMsg","友情提示，库存不足，请重新下单或者，更改购买数量！");
                // 返回错误页面
                return "tradeFail";
            }
            // 商品的验价：
            /**
             * 很重要！
             * 1. 订单的价格(orderPrice) 与 商品的实时价格（skuInfo.price）;
             * 2. 根据skuId 查询skuInfo.price 。 orderPrice 与 skuInfo.price 比较用哪个方法？{}
             * 3. 价格不一致！友情提示，价格有变动，请重新下单！ 修改loadCartCache(userId);
             */
        }
        // 1. 将数据从前台得到。保存到数据库中！
        // 调用服务层 添加数据 搭建orderService
        orderInfo.setUserId(userId);
        String orderId = orderService.saveOrder(orderInfo);
        // 将比较后的code 从redis 中 删除
        orderService.delTradeCode(userId);
        // 支付 根据orderId
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }

//      请求参数 orderId，wareSkuMap
    @RequestMapping("orderSplit")
    @ResponseBody
    public String orderSplit(HttpServletRequest request){
        // 获取orderId，wareSkuMap
        String orderId = request.getParameter("orderId");
        // [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        String wareSkuMap = request.getParameter("wareSkuMap");
        // 写拆单方案，调用服务层的方法。将orderId，wareSkuMap传入拆单方法中即可！
        // 组装子订单集合的json 字符串
        List<Map> wareMapList=new ArrayList<>();
        // 获取拆单之后子订单集合
        List<OrderInfo> orderInfoList = orderService.splitOrder(orderId,wareSkuMap);
        // 循环子订单集合
        for (OrderInfo orderInfo : orderInfoList) {
            // 将orderInfo 实体对象转换为map
            Map map = orderService.initWareOrder(orderInfo);
            // 添加到wareMapList集合中
            wareMapList.add(map);
        }

        return JSON.toJSONString(wareMapList);
    }
}
