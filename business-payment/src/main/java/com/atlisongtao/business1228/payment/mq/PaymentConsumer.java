package com.atlisongtao.business1228.payment.mq;

import com.alibaba.dubbo.config.annotation.Reference;

import com.atlisongtao.business1228.bean.PaymentInfo;
import com.atlisongtao.business1228.service.PaymentService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentConsumer {

    @Reference
    private PaymentService paymentService;

    // 消费消息队列
    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResultCheck(MapMessage mapMessage) throws JMSException {

        // 从消息队列中取数据
        String outTradeNo = mapMessage.getString("outTradeNo");
        int delaySec = mapMessage.getInt("delaySec");
        int checkCount = mapMessage.getInt("checkCount");

        // 业务逻辑调用！
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        // result = true ,表示支付成功，否则失败！
        boolean result = paymentService.checkPayment(paymentInfo);
        System.out.println("认证结果"+result);
        // 判断当前是否支付 15 秒check 一次， 总共check 3 !
        if (!result && checkCount>0){
            // 调用发送 消息的方法 checkCount-- error!
            System.out.println("checkCount:="+checkCount);
            paymentService.sendDelayPaymentResult(outTradeNo,delaySec,checkCount-1);
        }


    }
}
