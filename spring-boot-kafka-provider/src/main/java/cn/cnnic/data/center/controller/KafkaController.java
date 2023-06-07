package cn.cnnic.data.center.controller;


import cn.cnnic.data.center.service.impl.KafkaSendResultHandler;
import cn.cnnic.data.center.util.SnowFlake;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/kafka")
@Slf4j
public class KafkaController {


    private static final String TOPIC_NAME = "user.register.topic1";

    // @Autowired
    // private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired // adminClien需要自己生成配置bean
    private AdminClient adminClient;



    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public KafkaController(KafkaTemplate<Object, Object> kafkaTemplate, KafkaSendResultHandler kafkaSendResultHandler) {
        this.kafkaTemplate = kafkaTemplate;
        //回调方法、异常处理
        this.kafkaTemplate.setProducerListener(kafkaSendResultHandler);
    }

    /**
     * 发送消息
     * @param phone
     */
    @GetMapping("/api/user/{phone}")
    public void sendMessage1(@PathVariable("phone") String phone) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phone", phone);

        kafkaTemplate.send(TOPIC_NAME, jsonObject.toJSONString()).addCallback(success -> {
            // 消息发送到的topic
            String topic = success.getRecordMetadata().topic();
            // 消息发送到的分区
            int partition = success.getRecordMetadata().partition();
            // 消息在分区内的offset
            long offset = success.getRecordMetadata().offset();
            System.out.println("发送消息成功---> 主题：" + topic );
            System.out.println("发送消息成功---> 分区：" + partition);
            System.out.println("发送消息成功---> 偏移：" + offset);
            System.out.println("-----------------------------------------");

        }, failure -> {
            System.out.println("发送消息失败:" + failure.getMessage());
        });

    }

    /**
     * 注解方式的事务
     * 要想使用注解事务，必须先配置KafkaTransactionManager
     * @param i
     */
    @GetMapping("/kafka/transaction1")
    @Transactional(rollbackFor = RuntimeException.class)
    public void sendMessage1(int i) {
        kafkaTemplate.send(TOPIC_NAME, "这个是事务里面的消息：1  i="+i);
        if (i == 0) {
            throw new RuntimeException("fail");
        }
        kafkaTemplate.send(TOPIC_NAME, "这个是事务里面的消息：2  i="+i);
    }







    // 事务发送消息
    @GetMapping("/test")
    public void test() {
        // 声明事务：后面报错消息不会发出去
        kafkaTemplate.executeInTransaction(operations -> {
            operations.send("topic1","test executeInTransaction");
            throw new RuntimeException("fail");
        });
    }


    /**
     * 发送指定 topic、指定分区、指定key,指定时间
     * @param param
     */
    @GetMapping("/send/key/{param}")
    public void sendMessage(@PathVariable("param") String param) {

        for (int i = 0; i < 20; i++) {

            String mess ="~~~~sssssssssssssss "+i;
            //发送带有时间戳的消息
            kafkaTemplate.send("topic.hangge.demo", 0, System.currentTimeMillis(), "key1", mess);
        }

    }

    /**
     * 发送指定 topic、指定分区、指定key,指定时间
     * @param param
     */
    @GetMapping("/send/key2/{param}")
    public void sendMessage2(@PathVariable("param") String param) {

        for (int i = 100; i < 120; i++) {

            String mess =param +i;
            //发送带有时间戳的消息
            kafkaTemplate.send("topic.hangge.demo",   "key2", mess);
        }

    }


    @GetMapping("/send2/{msg}")
    public String send2(@PathVariable("msg") String msg) {
        kafkaTemplate.send("kafka_topic_001", msg);
        return "success";
    }


    @GetMapping("/send3/{msg}")
    public String send3(@PathVariable("msg") String msg) {
        kafkaTemplate.send("", msg);
        return "success";
    }
    @GetMapping("/api/curr/{phone}")
    public void sendMessage3(@PathVariable("phone") String phone) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phone", phone);

        kafkaTemplate.send("topic.cnnic.sdnsd", jsonObject.toJSONString()).addCallback(success -> {
            // 消息发送到的topic
            String topic = success.getRecordMetadata().topic();
            // 消息发送到的分区
            int partition = success.getRecordMetadata().partition();
            // 消息在分区内的offset
            long offset = success.getRecordMetadata().offset();
            System.out.println("发送消息成功---> 主题：" + topic );
            System.out.println("发送消息成功---> 分区：" + partition);
            System.out.println("发送消息成功---> 偏移：" + offset);
            System.out.println("-----------------------------------------");

        }, failure -> {
            System.out.println("发送消息失败:" + failure.getMessage());
        });

    }


    @GetMapping("/send5/{msg}")
    public String send5(@PathVariable("msg") String msg) {
        kafkaTemplate.send("kafka2", msg);
        return "success";
    }


    @GetMapping("/send6/{msg}")
    public String send6(@PathVariable("msg") String msg) {

        for (int i = 0; i < 40; i++) {
            long snowId = SnowFlake.getId();
            //生成业务唯一key
            String key =  "cnnic-sdnsd-"+snowId;

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", msg+" "+i);

            kafkaTemplate.executeInTransaction(operations -> {
               // kafkaTemplate.send("topic.hangge.demo_11", jsonObject.toJSONString());
                kafkaTemplate.send("topic.ppp", key, jsonObject.toJSONString());
               // throw new RuntimeException("fail");

                //入库

                return true;
            });
        }

      //  KeyedMessage

        return "success";
    }


}