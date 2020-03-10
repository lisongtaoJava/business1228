package com.atlisongtao.business1228.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;

import com.atlisongtao.business1228.bean.enums.ProcessStatus;
import com.atlisongtao.business1228.service.OrderService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;

/*
    消费消息
 */
@Component
public class OrderConsumer {

    @Reference
    private OrderService orderService;

    // 如何利用消息监听器工厂来监听是否由消息产生
    // destination 表示要监听的哪个消息队列
    // containerFactory 消息监听器工厂
    // 消息队列做什么事？更新orderInfo 状态
    // 获取消息队列的结果
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(ActiveMQMapMessage mapMessage ) throws JMSException {
        // 什么情况下更新订单？
        String result = mapMessage.getString("result");
        String orderId = mapMessage.getString("orderId");
        // 收拾它！ install - interface！
        // 当支付结果为success
        if ("success".equals(result)){
            // 准备更新订单状态 ，调用服务 ,根据订单的id 更新进程状态，订单状态。
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
            // 发送减库存的通知
            orderService.sendOrderStatus(orderId);
            orderService.updateOrderStatus(orderId,ProcessStatus.NOTIFIED_WARE);
        }
    }
    // consumeSkuDeduct
    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(ActiveMQMapMessage mapMessage ) throws JMSException {
        // 什么情况下更新订单？
        String status = mapMessage.getString("status");
        String orderId = mapMessage.getString("orderId");

        if ("DEDUCTED".equals(status)){
            // 更新订单的状态 ProcessStatus.WAITING_DELEVER
            orderService.updateOrderStatus(orderId,ProcessStatus.DELEVERED);
        }
    }
}
