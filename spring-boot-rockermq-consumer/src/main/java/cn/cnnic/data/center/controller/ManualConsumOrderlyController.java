package cn.cnnic.data.center.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class ManualConsumOrderlyController {

    public static void main(String[] args) throws MQClientException {
        String  TOPIC  ="sdnsd-topic";

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group-cnnic-consum");
        consumer.setNamesrvAddr("192.168.81.133:9876");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        //设置消费线程数大小取值范围都是 [1, 1000]。
        //consumeThreadMin 默认是20     consumeThreadMax 默认是64
        consumer.setConsumeThreadMin(2);
        consumer.setConsumeThreadMax(4);

        //批量消费最大消息条数，取值范围: [1, 1024]。默认是1
        consumer.setConsumeMessageBatchMaxSize(6);

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
        consumer.setPullInterval(5000); //拉取消息的时间间隔

        //消费者去broker拉取消息时，一次拉取多少条。取值范围: [1, 1024]。默认是32 。可选配置
        consumer.setPullBatchSize(32); //去broker拉取消息数

        //消息重试次数，超过进入死信队列
        consumer.setMaxReconsumeTimes(2);

        //顺序消费 重试时间间隔
        // 顺序消息消费失败的消费重试时间间隔，单位毫秒，默认为1000，其取值范围为[10,30000]
        consumer.setSuspendCurrentQueueTimeMillis(2000);


        consumer.subscribe(TOPIC, "tag_A");
        //如何分配消息队列给客户端，包括 AllocateMessageQueueByConfig根据配置分配消息队列、
        // AllocateMessageQueueAveragelyByCircle环状分配消息队列、
        //AllocateMessageQueueByMachineRoom平均分配消息队列、
        // AllocateMessageQueueAveragely 平均分配消息队列，也是默认分配算法。
        consumer.setAllocateMessageQueueStrategy(new AllocateMessageQueueAveragely());
        consumer.registerMessageListener(new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) {
                log.info("收到消息：条数：{} 线程：{},  内容：{}", list.size(), Thread.currentThread().getName(), list);
                MessageQueue queue = consumeOrderlyContext.getMessageQueue();
                int index = queue.getQueueId();
                log.info("当前队列： {}", index);
                try {
                    final LocalDateTime now = LocalDateTime.now();

                    for (int i = 0; i < list.size(); i++) {
                        MessageExt message = list.get(i);

                        //逐条消费
                        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
                        System.out.println("当前时间："+now+"， messageId: " + message.getMsgId() + ",topic: " +
                                message.getTopic()  + ",messageBody: " + messageBody);

                        log.info(messageBody);

                       // int aa = 1 / 0;
                        //模拟消费失败
                        if ("hello qiang 1".equals(messageBody)) {
                            log.info("傻了吧");
                            int a = 1 / 0;
                        }

                    }
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
                log.info("---------------------"+Thread.currentThread().getName()+"----------------------------");
                return ConsumeOrderlyStatus.SUCCESS;
                // return ConsumeConcurrentlyStatus.RECONSUME_LATER;


            }


        });
        consumer.start();
        System.out.println("启动成功--------------------------");
    }
}
