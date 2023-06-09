package cn.cnnic.data.center.controller;

import cn.cnnic.data.center.dto.OrderDto;
import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 顺序发送、顺序消费测试
 */
public class ManualOrderlyController {

    public static void main(String[] args) throws MQBrokerException, RemotingException, InterruptedException, MQClientException {
        String TOPIC = "sdnsd-topic";

        // 创建生产者对象
        DefaultMQProducer p = new DefaultMQProducer("prod-cnnic-group");
        p.setNamesrvAddr("192.168.81.133:9876");

        p.setRetryTimesWhenSendAsyncFailed(2);
        p.setRetryTimesWhenSendFailed(2);
        p.start();

        // 生成事务ID
        List<Message> mlist = new ArrayList<>();
        //for (int i = 0; i < 2; i++) {
          //  String orderId = UUID.randomUUID().toString().replace("-", "");
            Long deliverTimeStamp = System.currentTimeMillis() + 10L * 60 * 1000;

            //String orderId = "58ce4293c055458795c4c671add8ba94";
            String orderId = "09656c55f0804ec5acaee5531771f911";

            //=======================模拟订单=============================
            OrderDto orderDto1 = new OrderDto();
            orderDto1.setId(orderId);
            orderDto1.setOperType("notice");
            orderDto1.setMsg("青岛啤酒K8 24* 2");


        //1
            Message mm = new Message();
            mm.setTopic(TOPIC);
            mm.setBody(JSONObject.toJSONBytes(orderDto1));
            mm.setTags("tag_A");
            mm.setKeys("keys:" + orderId);
            //支持任意时间延时
            //mm.setDelayTimeSec(7);
            mm.putUserProperty("expireTime", String.valueOf(System.currentTimeMillis() + 1000 * 60));
            mlist.add(mm);

            //=======================模拟订单=============================

            System.out.println("orderId:::" + orderId);

            // SendResult sendResult = p.send(mm);
            // }

            /**
             SendResult sendResult = p.send(mlist);
             System.out.println(sendResult.getSendStatus());
             **/

            /*
             * MessageQueueSelector 是消息队列选择器, 这个作用是选择消息发送到哪个队列里面去
             * @param 参数1 : msg 你发送的消息
             * @param 参数2 : selector  消息Queue选择器,
             * @param 参数3: args 传给 消息队列选择器 使用的参数,SelectMessageQueueByHash 中select方法的第三个参数就是这个值传过去的
             */
            SendResult sendResult = p.send(mm, new SelectMessageQueueByHash() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    // System.out.println("arg:::"+arg);
                    Integer id = Math.abs(arg.hashCode());
                    int index = id % mqs.size();
                    System.out.println("id=" + id + ", index=" + index + ", mqs.size=" + mqs.size());
                    return mqs.get(index);
                }
            }, orderId);
       // }



        p.shutdown();
    }
}
