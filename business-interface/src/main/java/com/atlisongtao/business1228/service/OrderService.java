package com.atlisongtao.business1228.service;


import com.atlisongtao.business1228.bean.OrderInfo;
import com.atlisongtao.business1228.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {
    /**
     *  保存订单
     * @param orderInfo
     * @return
     */
    String  saveOrder(OrderInfo orderInfo);

    // 添加code
    String getTradeNo(String userId);
    // 比较code
    boolean checkTradeCode(String userId, String tradeCodeNo);
    // 删除code
    void delTradeCode(String userId);
    // 验证库存
    boolean checkStock(String skuId, Integer skuNum);
    // 根据orderId 查询orderInfo
    OrderInfo getOrderInfo(String orderId);
    // 根据订单Id 更新数据
    void updateOrderStatus(String orderId, ProcessStatus processStatus);
//    发送减库存通知
    void sendOrderStatus(String orderId);
    // 获取过期订单
    List<OrderInfo> getExpiredOrderList();
    // 关闭过期订单
    void execExpiredOrder(OrderInfo orderInfo);
    // 将orderInfo对象转换为Map集合
    Map initWareOrder(OrderInfo orderInfo);
    // 根据orderId 将原始订单进行拆单！
    List<OrderInfo> splitOrder(String orderId, String wareSkuMap);
}
