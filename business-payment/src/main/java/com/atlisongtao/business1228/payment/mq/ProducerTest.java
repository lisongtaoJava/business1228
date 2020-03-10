package com.atlisongtao.business1228.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;
import javax.management.Query;

/**
 * 消息提供者
 */
public class ProducerTest {
    public static void main(String[] args) throws JMSException {
        //创建连接工厂
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.192.128:61616");

        //创建连接
        Connection connection = activeMQConnectionFactory.createConnection();

        //打开连接
        connection.start();

        //创建session 第一个 参数表示是否开启事务
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        //创建队列
        Queue queue = session.createQueue("Atlisongtao");

        //创建消息提供者
        MessageProducer producer = session.createProducer(queue);

        //创建消息对象
        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText("发送消息");

        //发送消息
        producer.send(activeMQTextMessage);

        //关闭对象
        producer.close();
        session.close();
        connection.close();
    }
}
