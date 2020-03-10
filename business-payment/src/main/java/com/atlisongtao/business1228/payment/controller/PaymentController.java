package com.atlisongtao.business1228.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;

import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;

import com.atlisongtao.business1228.bean.OrderInfo;
import com.atlisongtao.business1228.bean.PaymentInfo;
import com.atlisongtao.business1228.bean.enums.PaymentStatus;
import com.atlisongtao.business1228.payment.config.AlipayConfig;
import com.atlisongtao.business1228.service.OrderService;
import com.atlisongtao.business1228.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;

    @Reference
    private PaymentService paymentService;

    @Autowired
    private AlipayClient alipayClient;

    @RequestMapping("index")
    public String index(HttpServletRequest request){
        // 获取orderId
        String orderId = request.getParameter("orderId");
        // 总金额：根据orderId 查询orderInfo 调用服务层
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        // 保存orderId
        request.setAttribute("orderId",orderId);
        // 保存总金额
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        // 返回一个页面，让用户选择支付渠道
        return "index";
    }
    // 生成支付二维码
    @RequestMapping("alipay/submit")
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response){
        // 交易信息的保存 paymentInfo --> orderInfo
        // 得到orderInfo 的信息
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        // 创建一个paymentInfo
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("测试给0715的帅哥，美女购买手机");
        paymentService.savyPaymentInfo(paymentInfo);
        // 生成二维码
        // 制作签名，参数如何处理！ AlipayClient 注入容器中！参数放入配置文件中即可！
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        // 设置同步回调 给用户看
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        // 设置异步回调 给电商看
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        // 设置参数 setBizContent() json 字符串！
        HashMap<String, String> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getTotalAmount().toString());
        map.put("subject","测试给0715小伙伴买手机");
        // 将map 转换为json
        String mapJson = JSON.toJSONString(map);
        alipayRequest.setBizContent(mapJson);

//        alipayRequest.setBizContent("{" +
//                "    \"out_trade_no\":\"20150320010101001\"," +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
//                "    \"total_amount\":88.88," +
//                "    \"subject\":\"Iphone6 16G\"," +
//                "    \"body\":\"Iphone6 16G\"," +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\"," +
//                "    \"extend_params\":{" +
//                "    \"sys_service_provider_id\":\"2088511833207846\"" +
//                "    }"+
//                "  }");//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");

        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);
        return form;
    }

    // 支付宝同步回调
    @RequestMapping("alipay/callback/return")
    public String callback(){
        return "redirect:"+AlipayConfig.return_order_url;
    }
    // 异步回调 通知电商的支付结果  success 时才能成功！面试题：fail 失败！{ 网络异常！}
    @RequestMapping("alipay/callback/notify")
    public String callbackNotify(@RequestParam Map<String,String> paramMap,HttpServletRequest request) throws AlipayApiException {
        // 将异步通知中收到的所有参数都存放到map中 url 上的所有参数，我们可以使用springMVC的那个注解得？ springmvc 接收传值方式有几种？
        // <input type="texy" name = "userName1">  UserInfo ()
//        Map<String, String> paramsMap = ... //将异步通知中收到的所有参数都存放到map中
        PaymentInfo paymentInfoUPD = new PaymentInfo();
        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key,AlipayConfig.charset , AlipayConfig.sign_type); //调用SDK验证签名
        if(flag){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            // 获取交易状态
            String trade_status = paramMap.get("trade_status"); // 校验交易状态
            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
                // 根据交易第三方编号查询当前的交易记录
                String out_trade_no = paramMap.get("out_trade_no");
                // select *  from paymentInfo where out_trade_no=？
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOutTradeNo(out_trade_no);
                // 调用方法
                PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);
                // 假如说交易状态是成功的！但是，在交易记录表中，该条记录的支付状态或者是进程状态已经结束了，或者是已经付款完成，那么失败！
                if (paymentInfoQuery.getPaymentStatus()==PaymentStatus.ClOSED || paymentInfoQuery.getPaymentStatus()==PaymentStatus.PAID ){
                    return "fail";
                }
                // 更新支付交易表中的PaymentStatus= PaymentStatus.PAID
                // update paymentInfo set PaymentStatus = PaymentStatus.PAID where out_trade_no = ?

                // 给要更新的对象进行赋值
                paymentInfoUPD.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoUPD.setCallbackTime(new Date());
//                paymentInfoUPD.setOutTradeNo(out_trade_no);
//                paymentInfoUPD.setCallbackContent(paramMap.toString());
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUPD);

                // 如果支付成功，告诉电商，哪个orderId ，result
                paymentService.sendPaymentResult(paymentInfoUPD,"success");
                return "success";
            }
        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            paymentService.sendPaymentResult(paymentInfoUPD,"fail");
            return "fail";
        }
        return "fail";
    }
    // 手写控制器
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,String result){
        // 调用发送支付结果通知
        paymentService.sendPaymentResult(paymentInfo,result);
        return "OK";
    }

    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        // 获取paymentInfo对象中的outTradeN0
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);

        boolean flag = paymentService.checkPayment(paymentInfoQuery);
        return ""+flag;
    }


}
