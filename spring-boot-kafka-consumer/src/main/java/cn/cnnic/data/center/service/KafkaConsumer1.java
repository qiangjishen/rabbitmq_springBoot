package cn.cnnic.data.center.service;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer1 {

    @KafkaListener(groupId = "top_group_1",topics = {"kafka_topic_001","kafka_89757"})
    public String consumerTopic(ConsumerRecord<String, String> record, Consumer consumer){
        String msg = record.value();
        System.out.println("------------------- consumerTopic 收到消息：" + msg + "-------------------");
        consumer.commitAsync();
        return "received msg is " + msg;
    }

    @KafkaListener(groupId = "top_group_1",topics = "kafka1")
    @SendTo("kafka_89757")
    public String consumerTopic2(ConsumerRecord<String, String> record, Consumer consumer){
        String msg = record.value();
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

    @KafkaListener(topics = "plc1")
    public String consumerTopic_plc1(ConsumerRecord<String, String> record, Consumer consumer){
        String msg = record.value();
        System.out.println("------------------- consumerTopic_plc1 收到消息：" + msg + "-------------------");
        consumer.commitAsync();
        return "received msg is " + msg;
    }
}

