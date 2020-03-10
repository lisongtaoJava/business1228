package com.atlisongtao.business1228.order.task;

import com.alibaba.dubbo.config.annotation.Reference;

import com.atlisongtao.business1228.bean.OrderInfo;
import com.atlisongtao.business1228.service.OrderService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@EnableScheduling
@Component
public class OrderTask {

    @Reference
    private OrderService orderService;

    // 直接定义方法 ，必须要注意在定义方法上添加一个注解
    @Scheduled(cron = "5 * * * * ?")
    public void sayHi(){
        System.out.println(Thread.currentThread().getName()+"sayHi");

    }
    // 每个五秒执行一次！
    @Scheduled(cron = "0/5 * * * * ?")
    public void sayHello(){
        System.out.println(Thread.currentThread().getName()+"sayHello");

    }

    @Scheduled(cron = "0/20 * * * * ?")
    public void closeOrder(){
        // 获取当前系统时间
        long starttime  = System.currentTimeMillis();
        // 关闭过期订单 得到过期订单列表
      List<OrderInfo> orderInfoList = orderService.getExpiredOrderList();

      // 循环过期订单，进行关闭即可！
        for (OrderInfo orderInfo : orderInfoList) {
            // 关闭过期订单
            orderService.execExpiredOrder(orderInfo);
        }
        long costtime   = System.currentTimeMillis()-starttime;

        System.out.println("一共处理"+orderInfoList.size()+"个订单 共消耗"+costtime+"毫秒");

    }
}
