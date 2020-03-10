package com.atlisongtao.business1228.payment.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atlisongtao.business1228.bean.PaymentInfo;
import com.atlisongtao.business1228.bean.enums.PaymentStatus;
import com.atlisongtao.business1228.config.ActiveMQUtil;
import com.atlisongtao.business1228.payment.mapper.PaymentMapper;
import com.atlisongtao.business1228.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;

@Service
public class PaymentServiceImpl implements PaymentService {
    /**
     * 交易信息的保存
     *
     * @param paymentInfo
     */
    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Autowired
    private AlipayClient alipayClient;

    @Override
    public void savyPaymentInfo(PaymentInfo paymentInfo) {
        // 数据保存
        paymentMapper.insertSelective(paymentInfo);
    }

    /**
     * 根据out_trade_no 查询PaymentInfo
     *
     * @param paymentInfo
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo) {
        // select *  from paymentInfo where out_trade_no=？
       return   paymentMapper.selectOne(paymentInfo);
    }

    /**
     * 按照out_trade_no 更新数据
     *
     * @param out_trade_no
     * @param paymentInfoUPD
     */
    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD) {
        //  update paymentInfo set PaymentStatus = PaymentStatus.PAID where out_trade_no = ?
        Example example = new Example(PaymentInfo.class);
        // outTradeNo 实体类的属性名 ，而不是数据库的字段名
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);

        paymentMapper.updateByExampleSelective(paymentInfoUPD,example);
    }

    /**
     * 支付宝将支付结果发送给订单模块
     *
     * @param paymentInfo
     * @param result
     */
    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
        // 创建连接
        Connection connection = activeMQUtil.getConnection();

        // 打开连接
        try {
            connection.start();
            // 创建session 表示开启事务
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            // 创建消息提供者
            MessageProducer producer = session.createProducer(queue);
            // 创建发送消息对象
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId",paymentInfo.getOrderId());
            mapMessage.setString("result",result);

            // 发送消息
            producer.send(mapMessage);
            // 必须提交
            session.commit();
            // 关闭
            producer.close();
            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    /**
     * 通过out_trade_no 查询支付结果！
     *
     * @param paymentInfoQuery
     * @return
     */
    @Override
    public boolean checkPayment(PaymentInfo paymentInfoQuery) {
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfoQuery.getOutTradeNo());
        request.setBizContent(JSON.toJSONString(map));
//        request.setBizContent("{" +
//                "\"out_trade_no\":\"20150320010101001\"," +
//                "  }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // 说明第三季交易记录在支付宝中存在
        if(response.isSuccess()){
            // 交易状态
            if ("TRADE_SUCCESS".equals(response.getTradeStatus()) || "TRADE_FINISHED".equals(response.getTradeStatus())){
                System.out.println("调用成功--并支付成功！");
                // 修改交易状态
                PaymentInfo paymentInfoUPD = new PaymentInfo();
                paymentInfoUPD.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfo(paymentInfoQuery.getOutTradeNo(),paymentInfoUPD);
                return true;
            }
        } else {
            System.out.println("调用失败---并没有支付！");
        }
        return false;
    }

    /**
     * @param outTradeNo 第三方交易编号
     * @param delaySec   延迟的时间
     * @param checkCount 检查的次数
     */
    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {
        // 创建工厂
        Connection connection = activeMQUtil.getConnection();
        // 打开连接
        try {
            connection.start();
            // 创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            // 创建消息提供者
            MessageProducer producer = session.createProducer(queue);

            // 创建消息对象
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo",outTradeNo);
            mapMessage.setInt("delaySec",delaySec);
            mapMessage.setInt("checkCount",checkCount);

            // 设置一下开启延迟队列属性
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);
            // 准备发送 消息
            producer.send(mapMessage);
            session.commit();

            producer.close();
            session.close();
            connection.close();


        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    /**
     * 根据orderId 关闭交易记录数据
     *
     * @param orderId
     */
    @Override
    public void closePayment(String orderId) {
        // 创建Example
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId",orderId);
        // update paymentInfo set paymentStatus = close where orderId = ?
        // 创建paymentInfo
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);
        paymentMapper.updateByExampleSelective(paymentInfo,example);
    }
}
