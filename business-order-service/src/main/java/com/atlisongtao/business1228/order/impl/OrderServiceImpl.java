package com.atlisongtao.business1228.order.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;

import com.atlisongtao.business1228.bean.OrderDetail;
import com.atlisongtao.business1228.bean.OrderInfo;
import com.atlisongtao.business1228.bean.enums.OrderStatus;
import com.atlisongtao.business1228.bean.enums.ProcessStatus;
import com.atlisongtao.business1228.config.ActiveMQUtil;
import com.atlisongtao.business1228.config.RedisUtil;
import com.atlisongtao.business1228.order.mapper.OrderDetailMapper;
import com.atlisongtao.business1228.order.mapper.OrderInfoMapper;
import com.atlisongtao.business1228.service.OrderService;
import com.atlisongtao.business1228.service.PaymentService;
import com.atlisongtao.business1228.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {
    /**
     * 保存订单
     *
     * @param orderInfo
     * @return
     */
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Reference
    private PaymentService paymentService;

    @Override
    public String saveOrder(OrderInfo orderInfo) {

        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        // 设置总金额
        orderInfo.sumTotalAmount();
        orderInfo.setTotalAmount(orderInfo.getTotalAmount());
        // 数据库表需要创建时间
        orderInfo.setCreateTime(new Date());
        // 设置过期时间
        Calendar calendar = Calendar.getInstance();
        // 在当前时间上进行+1天
        calendar.add(Calendar.DATE,1);

        orderInfo.setExpireTime(calendar.getTime());

        // 设置下第三方交易编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        // 添加orderInfo
        orderInfoMapper.insertSelective(orderInfo);
        // 添加订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        // 循环添加
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            // 添加数据
            orderDetailMapper.insertSelective(orderDetail);
        }
        return orderInfo.getId();
    }
    // 生成code 方法
    public String getTradeNo(String userId){
        // 存储到redis key=由userId组成
        Jedis jedis = redisUtil.getJedis();
        // 定义key
        String tradeNoKey="user:"+userId+":tradeCode";
        // 生成code
        String tradeCode  = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        // 关闭
        jedis.close();
        return tradeCode;
    }
    // 比较code
    public boolean checkTradeCode(String userId,String tradeCodeNo){
        // 根据userId 取得redis中的数据
        Jedis jedis = redisUtil.getJedis();
        // 定义key
        String tradeNoKey="user:"+userId+":tradeCode";
        String tradeCode  = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCode!=null && !"".equals(tradeCode)){
            if (tradeCode.equals(tradeCodeNo)){
                return true;
            }else {
                return  false;
            }
        }
        return false;
    }
    // 删除code
    public void delTradeCode(String userId){
        // 存储到redis key=由userId组成
        Jedis jedis = redisUtil.getJedis();
        // 定义key
        String tradeNoKey="user:"+userId+":tradeCode";
        // 删除key
        jedis.del(tradeNoKey);

        jedis.close();
    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        // 真正的调用库存系统的接口 httpClient
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if ("1".equals(result)){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        // 获取订单详情
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        // 商品详情添加到orderInfo
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {
        // update orderInfo set processStatus = ？ and orderStatus = ？ where orderId = ？
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    @Override
    public void sendOrderStatus(String orderId) {
        // 创建连接
        Connection connection = activeMQUtil.getConnection();
        // 将要转换的字符串做成一个方法来处理
        String orderJson= initWareOrder(orderId);
        // 打开连接
        try {
            connection.start();
            // 创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue queue = session.createQueue("ORDER_RESULT_QUEUE");
            // 创建消息提供者
            MessageProducer producer = session.createProducer(queue);
            // 创建消息对象
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            // json 字符串！
            activeMQTextMessage.setText(orderJson);
            // 准备发送消息
            producer.send(activeMQTextMessage);
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

    @Override
    public List<OrderInfo> getExpiredOrderList() {
        // 查询过期订单 过期时间<当前时间 and 未付款
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("processStatus",ProcessStatus.UNPAID).andLessThan("expireTime",new Date());
        List<OrderInfo> orderInfoList = orderInfoMapper.selectByExample(example);
        return orderInfoList;
    }

    @Async
    @Override
    public void execExpiredOrder(OrderInfo orderInfo) {
        // 关闭过期订单 {update状态} orderInfo
        updateOrderStatus(orderInfo.getId(),ProcessStatus.CLOSED);
        // paymentInfo 交易记录
        paymentService.closePayment(orderInfo.getId());
    }

    // 准备返回字符串{Json}
    private String initWareOrder(String orderId) {
        // 根据orderId 查询orderInfo
        OrderInfo orderInfo = getOrderInfo(orderId);
        // 将 orderInfo 转换为map
        Map map = initWareOrder(orderInfo);
        return JSON.toJSONString(map);
    }
    // 将 orderInfo 转换为map
    public Map initWareOrder(OrderInfo orderInfo) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody","测试你怎么了？");
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        map.put("wareId",orderInfo.getWareId());
        //        details : 订单明细集合的json字符串
        // 创建一个存储订单明细的集合
        ArrayList<Map> arrayList = new ArrayList<>();
        // 获取orderDetailList
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            // 循环遍历取出里面的值  {skuId:101,skuNum:1,skuName:’小米手64G’}
            // 创建一个map
            HashMap<String, Object> detailMap = new HashMap<>();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailMap.put("skuName",orderDetail.getSkuName());
            arrayList.add(detailMap);
        }
        map.put("details",orderDetailList);
        return map;
    }

    @Override
    public List<OrderInfo> splitOrder(String orderId, String wareSkuMap) {
        // 定义子订单集合
        List<OrderInfo> subListOrderInfoList = new ArrayList<>();
        // 原始订单是谁？通过orderId 查询得到
        OrderInfo orderInfoOrigin  = getOrderInfo(orderId);
        // 将 wareSkuMap [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}] 转换为我们需要的集合类型
        List<Map> mapList = JSON.parseArray(wareSkuMap, Map.class);
        // 循环遍历
        for (Map map : mapList) {
            // 取出仓库Id
            String wareId = (String) map.get("wareId");
            // 取出skuIds
            List<String> skuIds = (List<String>) map.get("skuIds");
            // 创建一个新的子订单
            OrderInfo subOrderInfo = new OrderInfo();
            // 属性拷贝
            BeanUtils.copyProperties(orderInfoOrigin,subOrderInfo);
            // 注意主键不能重复！
            subOrderInfo.setId(null);
            // 父订单Id
            subOrderInfo.setParentOrderId(orderInfoOrigin.getId());
            // 仓库Id
            subOrderInfo.setWareId(wareId);
            // 获取到最新的orderDetail
            List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
            // 定义一个新的子订单集合明细
            ArrayList<OrderDetail> subOrderDetailList = new ArrayList<>();
            // 循环原始订单的订单明细
            for (OrderDetail orderDetail : orderDetailList) {
                // 循环拆分之后的skuIds ，然后跟原始的订单明细进行匹配
                for (String skuId : skuIds) {
                    if (skuId.equals(orderDetail.getSkuId())){
                        orderDetail.setId(null);
                        subOrderDetailList.add(orderDetail);
                    }
                }
            }
            // 将最新的子订单明细添加到子订单对象中
            subOrderInfo.setOrderDetailList(subOrderDetailList);
            // 价格
            subOrderInfo.sumTotalAmount();
            // 保存新的子订单集合
            saveOrder(subOrderInfo);
            // 将新的子订单添加到集合中！
            subListOrderInfoList.add(subOrderInfo);
        }
            // 更新原始订单的状态
            updateOrderStatus(orderId,ProcessStatus.SPLIT);
        // 返回子订单集合
        return subListOrderInfoList;
    }


}
