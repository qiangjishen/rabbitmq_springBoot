package cn.cnnic.data.center.service;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "sdnsd-topic",consumerGroup = "my-consumer-group",consumeMode = ConsumeMode.CONCURRENTLY, messageModel = MessageModel.CLUSTERING,  selectorExpression = "*")
public class Consumer implements RocketMQListener<MessageExt>, RocketMQPushConsumerLifecycleListener {
    @Override
    public void onMessage(MessageExt message) {
        System.out.println("消费者收到信息：==="+message);
    }

    //    该方法重写消息监听器的属性
    @Override
    public void prepareStart(DefaultMQPushConsumer defaultMQPushConsumer) {
        // 设置消费者消息重试次数
        defaultMQPushConsumer.setMaxReconsumeTimes(3);
        //        设置实例名称
        defaultMQPushConsumer.setInstanceName("mqconsumer1");
    }

}
