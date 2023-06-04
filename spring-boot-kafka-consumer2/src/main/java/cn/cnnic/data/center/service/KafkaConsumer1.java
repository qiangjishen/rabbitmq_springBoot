package cn.cnnic.data.center.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaConsumer1 {


    @KafkaListener(groupId = "top_group_55",topics = "user.register.topic")
    public String consumerTopic5(String record, Consumer consumer){
        String msg = record;
        System.out.println("C2------------------- user.register.topic1 收到消息：" + msg + "-------------------");
        consumer.commitAsync();

        return "received msg is " + msg;
    }

    // 消费监听
    @KafkaListener(id = "consumer88", topics = "topic.hangge.demo11")
    public void listen88(String data) {
        System.out.println(data);
    }

    /** 手动提交offset
     * 声明consumerID为demo，方便kafkaserver打印日志定位请求来源，监听topicName为topic.quick.demo的Topic
     * clientIdPrefix设置clientId前缀， idIsGroup id为groupId：默认为true
     * concurrency: 在监听器容器中运行的线程数,创建多少个consumer，值必须小于等于Kafk Topic的分区数。大于分区数时会有部分线程空闲
     * topicPattern 匹配Topic进行监听(与topics、topicPartitions 三选一)
     *
     * @param record 消息内容
     * @param ack    应答
     * @author yh
     * @date 2022/5/10
     */
    @KafkaListener(id = "demoContainer", topics = "user.register.topic1", groupId = "mykafka2", idIsGroup = false, clientIdPrefix = "myClient1", concurrency = "${listen.concurrency:3}", containerFactory = "kafkaManualAckListenerContainerFactory", autoStartup = "false")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
        System.out.println(record);
        System.out.println(record.value());

        log.info("【接受到消息][线程ID:{} 消息内容：{}]", Thread.currentThread().getId(), record.value());
        // 消息处理下游绑定事务，成功消费后提交ack
        // 手动提交offset
        ack.acknowledge();
    }


    @KafkaListener(id = "demoContainer3", topics = "topic.cnnic.sdnsd", groupId = "mykafka3", idIsGroup = false, clientIdPrefix = "myClient1", concurrency = "${listen.concurrency:3}", containerFactory = "kafkaManualAckListenerContainerFactory", autoStartup = "false")
    public void listen3(ConsumerRecord<String, String> record, Acknowledgment ack) {
        System.out.println(record);
        System.out.println(record.value());

        log.info("【接受到消息][线程ID:{} 消息内容：{}]", Thread.currentThread().getId(), record.value());
        // 消息处理下游绑定事务，成功消费后提交ack
        // 手动提交offset
        ack.acknowledge();
    }


}

