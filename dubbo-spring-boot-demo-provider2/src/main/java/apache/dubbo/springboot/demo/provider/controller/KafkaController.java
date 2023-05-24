package apache.dubbo.springboot.demo.provider.controller;

import apache.dubbo.springboot.demo.provider.service.impl.KafkaSendResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaController {

    private static final String TOPIC_NAME = "user.register.topic";

   // @Autowired
   // private KafkaTemplate<String, Object> kafkaTemplate;

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
        kafkaTemplate.send(TOPIC_NAME, phone).addCallback(success -> {
            // 消息发送到的topic
            String topic = success.getRecordMetadata().topic();
            // 消息发送到的分区
            int partition = success.getRecordMetadata().partition();
            // 消息在分区内的offset
            long offset = success.getRecordMetadata().offset();
            System.out.println("发送消息成功:" + topic + "-" + partition + "-" + offset);

        }, failure -> {
            System.out.println("发送消息失败:" + failure.getMessage());
        });

    }
}
