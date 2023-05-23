package cn.cnnic.dataCenter.service.impl;


import cn.cnnic.dataCenter.dto.CustomerDto;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "${rocketmq.producer.topic1}",
        consumerGroup = "${rocketmq.producer.consumerGroup}",
        selectorExpression = "tag1")
public class SpringTxConsumer2 implements RocketMQListener<String>, RocketMQPushConsumerLifecycleListener {

    @Override
    public void onMessage(String msg) {
        log.info("2~~~~~接收到消息 -> {}", msg);
        CustomerDto dto = JSONObject.parseObject(msg, CustomerDto.class);
        log.info("2~~~~~"+dto.getRealName());
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer defaultMQPushConsumer) {
        defaultMQPushConsumer.setConsumeThreadMax(30);
        defaultMQPushConsumer.setConsumeThreadMin(20);
    }
}
