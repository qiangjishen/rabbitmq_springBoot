package cn.cnnic.data.center.service;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @author 徐一杰
 * @date 2022/10/31 14:04
 * kafka监听消息
 */
@RestController
public class KafkaConsumer {


    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;


    public KafkaConsumer(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    }
    /**
    public KafkaConsumer(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {

    }
     /**

    /**
     * 监听kafka消息
     *
     * @param record kafka的消息，用consumerRecord可以接收到更详细的信息，也可以用String message只接收消息
     * @param ack            kafka的消息确认
     *                       使用autoStartup = "false"必须指定id
     */
    @KafkaListener(topics = {"topic1", "topic2"}, errorHandler = "myKafkaListenerErrorHandler")
//    @KafkaListener(id = "${spring.kafka.consumer.group-id}", topics = {"topic1", "topic2"}, autoStartup = "false")
    public void listen1(ConsumerRecord<Object, Objects> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {

            //用于测试异常处理
            //int i = 1 / 0;

            // 打印出消息内容
            System.out.println("c1 消费："+record.topic()+"-"+record.partition()+"-"+record.value());

            //手动确认
            ack.acknowledge();
        } catch (Exception e) {
            System.out.println("消费失败：" + e);
        }
    }

    /**
     * 下面的方法可以手动操控kafka的队列监听情况
     * 先发送一条消息，因为autoStartup = "false"，所以并不会看到有消息进入监听器。
     * 接着启动监听器，/start/webGroup。可以看到有一条消息进来了。
     * pause是暂停监听，resume是继续监听
     *
     * @param listenerId consumer的group-id
     */
    @RequestMapping("/pause/{listenerId}")
    public void stop(@PathVariable String listenerId) {
        Objects.requireNonNull(kafkaListenerEndpointRegistry.getListenerContainer(listenerId)).pause();
    }

    @RequestMapping("/resume/{listenerId}")
    public void resume(@PathVariable String listenerId) {
        Objects.requireNonNull(kafkaListenerEndpointRegistry.getListenerContainer(listenerId)).resume();
    }

    @RequestMapping("/start/{listenerId}")
    public void start(@PathVariable String listenerId) {
        Objects.requireNonNull(kafkaListenerEndpointRegistry.getListenerContainer(listenerId)).start();
    }
}