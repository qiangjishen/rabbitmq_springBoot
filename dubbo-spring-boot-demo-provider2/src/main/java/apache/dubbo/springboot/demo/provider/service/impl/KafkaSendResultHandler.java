package apache.dubbo.springboot.demo.provider.service.impl;


import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;

@Component
public class KafkaSendResultHandler implements ProducerListener<Object, Object> {

    @Override
    public void onSuccess(ProducerRecord producerRecord, RecordMetadata recordMetadata) {
        System.out.println("消息发送成功：" + producerRecord.toString());
    }

    @Override
    public void onError(ProducerRecord producerRecord, @Nullable RecordMetadata recordMetadata, Exception exception) {
        System.out.println("消息发送失败：" + producerRecord.toString() + exception.getMessage());
    }
}
