package com.atlisongtao.business1228.payment.mq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * 消息消费者
 */
public class ConsumerTest {
    public static void main(String[] args) throws JMSException {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, "tcp://192.168.192.128:61616");

        //创建连接
        Connection connection = activeMQConnectionFactory.createConnection();

        //打开连接
        connection.start();
        //创建session
        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
        //消费哪一个队列
        Queue queue = session.createQueue("Atlisongtao");


        //创建消费者
        MessageConsumer consumer = session.createConsumer(queue);
        //消息监听器消费消息
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                //判断两个对象是否相同
                if (message instanceof TextMessage){
                    try {
                        String text = ((TextMessage) message).getText();

                        System.out.println("消息内容"+text);

                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
}
