package cn.cnnic.data.center.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rocket")
@Slf4j
public class ManualController {

    private  String  TOPIC  ="sdnsd.topic1";

    @RequestMapping(value = "/send",method = RequestMethod.GET)
    public SendResult sendMsgSelector(String msg) throws MQBrokerException, RemotingException, InterruptedException, MQClientException {

        // 创建生产者对象
        DefaultMQProducer p = new DefaultMQProducer("prod-cnnic-group1");
        p.setNamesrvAddr("192.168.81.133:9876");

        // 生成事务ID
        String orderId = UUID.randomUUID().toString().replace("-", "");

        Message mm = new Message();
        mm.setTopic(TOPIC);


        SendResult sendResult = p.send(mm, new MessageQueueSelector() {
            @Override
            public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                Integer id = (Integer) arg;
                int index = id % mqs.size();
                System.out.println("id="+id+", index="+index+", mqs.size="+mqs.size());
                return mqs.get(index);
            }
        }, orderId);



        return sendResult;
    }

    public static void main(String[] args) throws MQBrokerException, RemotingException, InterruptedException, MQClientException {
        String TOPIC = "sdnsd-topic";

        // 创建生产者对象
        DefaultMQProducer p = new DefaultMQProducer("prod-cnnic-group");
        p.setNamesrvAddr("192.168.81.133:9876");
        p.start();

        // 生成事务ID
        List<Message> mlist = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String orderId = UUID.randomUUID().toString().replace("-", "");
            Long deliverTimeStamp = System.currentTimeMillis() + 10L * 60 * 1000;


            String str = "Hello Everyone " + i;
            Message mm = new Message();

            mm.setTopic(TOPIC);
            mm.setBody(str.getBytes());
            mm.setTags("tag_A");
            mm.setKeys("keys:" + orderId);
            //支持任意时间延时
            //mm.setDelayTimeSec(7);
            mm.putUserProperty("expireTime", String.valueOf(System.currentTimeMillis() + 1000 * 60));

            mlist.add(mm);
            System.out.println("orderId:::" + orderId);

            // SendResult sendResult = p.send(mm);
       // }

        /**
         SendResult sendResult = p.send(mlist);
         System.out.println(sendResult.getSendStatus());
         **/

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
    }



        p.shutdown();
    }



}
