package cn.cnnic.data.center.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragelyByCircle;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.List;

@Slf4j
public class DeadQueueController {
    public static void main(String[] args) throws MQClientException {
        String  TOPIC  ="%DLQ%group-cnnic-consum";

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group-cnnic-consum-dead");
        consumer.setNamesrvAddr("192.168.81.133:9876");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        //设置消费线程数大小取值范围都是 [1, 1000]。
        //consumeThreadMin 默认是20     consumeThreadMax 默认是64
        consumer.setConsumeThreadMin(2);
        consumer.setConsumeThreadMax(4);

        //批量消费最大消息条数，取值范围: [1, 1024]。默认是1
        consumer.setConsumeMessageBatchMaxSize(2);

        //pullThresholdForTopic为每个topic在本地缓存最多的消息条数,取值范围[1, 6553500]，默认的-1
        consumer.setPullThresholdForTopic(1000);

        //pullThresholdSizeForTopic 是在topic级别限制了消息缓存的大小，单位为Mib，取值范围[1, 102400]，默认为-1
        consumer.setPullThresholdSizeForTopic(1000);

        //是拉消息本地队列缓存消息最大数 用于topic级别的流量控制，控制单位为消息个数，
        // 取值范围都是 [1, 65535]，默认是1000
        consumer.setPullThresholdForQueue(100);

        //是 topic级别缓存大小限制，取值范围 [1, 1024]，默认是100Mib,
        consumer.setPullThresholdSizeForQueue(100);

        //检查拉取消息的间隔时间，由于是长轮询，所以为 0，但是如果应用为了流控，也可以设置大于 0 的值，单位毫秒，取值范围: [0, 65535]
        consumer.setPullInterval(2000); //拉取消息的时间间隔

        //消费者去broker拉取消息时，一次拉取多少条。取值范围: [1, 1024]。默认是32 。可选配置
        consumer.setPullBatchSize(32); //去broker拉取消息数

        //消息重试次数，超过进入死信队列
        consumer.setMaxReconsumeTimes(2);


        consumer.subscribe(TOPIC, "*");
        //如何分配消息队列给客户端，包括 AllocateMessageQueueByConfig根据配置分配消息队列、AllocateMessageQueueAveragelyByCircle环状分配消息队列、
        //AllocateMessageQueueByMachineRoom平均分配消息队列、AllocateMessageQueueAveragely 平均分配消息队列，也是默认分配算法。
        consumer.setAllocateMessageQueueStrategy(new AllocateMessageQueueAveragelyByCircle());
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                log.info("死信队列：条数：{} 线程：{},  内容：{}", list.size(), Thread.currentThread().getName(), list);

                MessageExt m = list.get(0);
                log.info("具体：key:{}    context:{}    offset: {}",m.getKeys(), new String(m.getBody()), m.getQueueOffset());

                //MessageQueue queue = consumeConcurrentlyContext.getMessageQueue();
                //int index = queue.getQueueId();
                //log.info("当前队列： {}", index);

                log.info("---------------------"+Thread.currentThread().getName()+"----------------------------");
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                //return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
        consumer.start();

    }
}
