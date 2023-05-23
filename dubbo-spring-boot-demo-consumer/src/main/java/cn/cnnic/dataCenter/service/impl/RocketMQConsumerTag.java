package cn.cnnic.dataCenter.service.impl;

import cn.cnnic.dataCenter.dto.CustomerDto;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "CNNIC_ROCKETMQ_TOPIC1",
        consumerGroup = "transaction-consumer-group3",
        selectorExpression = "tag2")
public class RocketMQConsumerTag implements RocketMQListener<String> {
    @Override
    public void onMessage(String msg) {
        log.info("Tag接收到消息 -> {}", msg);
        CustomerDto dto = JSONObject.parseObject(msg, CustomerDto.class);
        log.info("Tag:"+dto.getRealName());
    }
}
