package cn.cnnic.data.center.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rocket")
@Slf4j
public class ManualController {


    @RequestMapping(value = "/send",method = RequestMethod.GET)
    public SendResult sendMsgSelector(String msg) throws MQBrokerException, RemotingException, InterruptedException, MQClientException {

        // 创建生产者对象
        DefaultMQProducer p = new DefaultMQProducer("prod-group1");

        // 生成事务ID
        String orderId = UUID.randomUUID().toString().replace("-", "");

        Message mm = new Message();


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


}
