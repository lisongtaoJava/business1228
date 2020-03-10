package com.atlisongtao.business1228.service;


import com.atlisongtao.business1228.bean.PaymentInfo;

public interface PaymentService {
    /**
     * 交易信息的保存
     * @param paymentInfo
     */
    void  savyPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 根据out_trade_no 查询PaymentInfo
     * @param paymentInfo
     * @return
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 按照out_trade_no 更新数据
     * @param out_trade_no
     * @param paymentInfoUPD
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD);

    /**
     * 支付宝将支付结果发送给订单模块
     * @param paymentInfo
     * @param result
     */
    void sendPaymentResult(PaymentInfo paymentInfo, String result);

    /**
     * 通过out_trade_no 查询支付结果！
     * @param paymentInfoQuery
     * @return
     */
    boolean checkPayment(PaymentInfo paymentInfoQuery);

    /**
     *
     * @param outTradeNo 第三方交易编号
     * @param delaySec 延迟的时间
     * @param checkCount 检查的次数
     */
    void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount);

    /**
     * 根据orderId 关闭交易记录数据
     * @param orderId
     */
    void closePayment(String orderId);
}
