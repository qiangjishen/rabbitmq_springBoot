package cn.cnnic.data.center.service;


import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.kafka.annotation.*;
import org.springframework.kafka.retrytopic.FixedDelayStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class KafkaConsumer1 {

    @KafkaListener(groupId = "top_group_1",topics = {"kafka_topic_001","kafka_89757_1"})
    public String consumerTopic(ConsumerRecord<String, String> record, Consumer consumer){
        String msg = record.value();
        System.out.println("------------------- consumerTopic 收到消息：" + msg + "-------------------");
        consumer.commitAsync();
        return "received msg is " + msg;
    }

    /** topicPartitions 可配置更加详细的监听信息，比如下面代码同样是同时监听 topic1 和 topic2，不同在于这次：
        监听 topic1 的 0 号分区
        监听 topic2 的 0 号和 1 号分区（其中 1 号分区的初始偏移量为 100）
     **/
    @KafkaListener(id = "consumer1",groupId = "my-group1",topicPartitions = {
            @TopicPartition(topic = "topic1", partitions = { "0" }),
            @TopicPartition(topic = "topic2", partitions = "2", partitionOffsets = @PartitionOffset(partition = "1", initialOffset = "100"))
    })
    public void listen1(String data) {
        System.out.println(data);
    }

    @KafkaListener(groupId = "top_group_1",topics = "kafka2", containerFactory = "kafkaManualAckListenerContainerFactory")
   // @SendTo("kafka_89757")
    public String consumerTopic2(ConsumerRecord<String, String> record, Consumer consumer, Acknowledgment ack){
        String msg = record.value();
        Map<String, TopicPartitionInfo> tos = consumer.listTopics();

        System.out.println("topic---->"+record.topic()+"     pation---->"+record.partition());
        org.apache.kafka.common.TopicPartition tp = new org.apache.kafka.common.TopicPartition("kafka2",0);
        System.out.println("currentLag---:"+consumer.currentLag(tp));

        System.out.println("------------------- consumerTopic2 收到消息：" + msg + "-------------------");
        consumer.commitAsync();
        return "received msg is " + msg;
    }

    @KafkaListener(groupId = "top_group_1",topics = "kafka_89757")
    public String consumerTopic3(ConsumerRecord<String, String> record, Consumer consumer){
        String msg = record.value();
        System.out.println("------------------- consumerTopic3 收到消息：" + msg + "-------------------");
        consumer.commitAsync();
        return "received msg is " + msg;
    }

    @KafkaListener(topics = "plc1", groupId = "top_group8")
    public String consumerTopic_plc1(ConsumerRecord<String, String> record, Consumer consumer){
        String msg = record.value();
        System.out.println("------------------- consumerTopic_plc1 收到消息：" + msg + "-------------------");
        consumer.commitAsync();
        return "received msg is " + msg;
    }



    @KafkaListener(groupId = "top_group_55",topics = "user.register.topic2")
    public String consumerTopic5(String record, Consumer consumer){
        String msg = record;
        System.out.println("------------------- user.register.topic1 收到消息：" + msg + "-------------------");
        //consumer.commitAsync();

        return "received msg is " + msg;
    }

    /**
     * attempts：重试次数，默认为3。
     *
     * @Backoff delay：消费延迟时间，单位为毫秒。
     *
     * @Backoff multiplier：延迟时间系数，此例中 attempts = 4， delay = 5000， multiplier = 2 ，则间隔时间依次为5s、10s、20s、40s，最大延迟时间受 maxDelay 限制。
     *
     * fixedDelayTopicStrategy：可选策略包括：SINGLE_TOPIC 、MULTIPLE_TOPICS
     */
    @KafkaListener(id = "consumer88", topics = "topic.hangge.demo_11", groupId = "top_group9")
    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 5000, multiplier = 2),
            fixedDelayTopicStrategy = FixedDelayStrategy.SINGLE_TOPIC
    )
    public void listen88(ConsumerRecord<String, String> record) {
        // 获取消息体
        String message = record.value();
        log.info("收到测试消息，message = {}, record={}", message, JSONObject.toJSONString(record));
        // todo  写重试业务消息
        throw new RuntimeException("test kafka exception");

    }

    @DltHandler
    public void dltHandler(ConsumerRecord<String, String> record) {
        log.info("进入死信队列 record={}", record.value());
        // todo  写重试多次失败后的业务，例如短信，报警，存数据库等。
    }


    @KafkaListener(id = "pppContainer", topicPartitions ={@TopicPartition(topic = "topic.ppp_1", partitions = {"0"}),}, groupId = "mykafka99", idIsGroup = false, clientIdPrefix = "myClient1", concurrency = "${listen.concurrency:2}", containerFactory = "kafkaManualAckListenerContainerFactory")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) throws InterruptedException {
        System.out.println(record);
        System.out.println(record.value());

        log.info("【分区：{}】【接受到消息][线程ID:{} 消息内容：{}]", record.partition(),Thread.currentThread().getId(), record.value());
        Thread.sleep(1000L);
        // 消息处理下游绑定事务，成功消费后提交ack
        // 手动提交offset
        ack.acknowledge();
    }



    @KafkaListener(id = "pppContainer2", topicPartitions ={@TopicPartition(topic = "topic.ppp", partitions = {"0,1"}),}, groupId = "mykafka99", idIsGroup = false, clientIdPrefix = "myClient1", concurrency = "${listen.concurrency:2}", containerFactory = "kafkaManualAckListenerContainerFactory")
    public void listen80(ConsumerRecord<String, String> record, Acknowledgment ack) throws InterruptedException {
        System.out.println(record);
        System.out.println(record.value());
        System.out.println("key: -->"+record.key());
        System.out.println("key hash: -->"+Math.abs(record.key().hashCode()));
        System.out.println("partition index: -->"+Math.abs(record.key().hashCode()) %8);

        //Math.abs(key.hashCode()) % partitions.size()


        log.info("【分区：{}】【接受到消息][线程ID:{} 消息内容：{}]", record.partition(), Thread.currentThread().getId(), record.value());
        Thread.sleep(1000L);
        // 消息处理下游绑定事务，成功消费后提交ack
        // 手动提交offset
        ack.acknowledge();
    }
}

