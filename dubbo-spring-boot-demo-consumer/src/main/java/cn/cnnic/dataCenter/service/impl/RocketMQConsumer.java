package cn.cnnic.dataCenter.service.impl;

import cn.cnnic.dataCenter.dto.CustomerDto;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "CNNIC_ROCKETMQ_TOPIC2",
        consumerGroup = "transaction-consumer-group2",
        selectorExpression = "*")
public class RocketMQConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String msg) {
        log.info("接收到消息2 -> {}", msg);
        CustomerDto dto = JSONObject.parseObject(msg, CustomerDto.class);
        log.info("名字2 -> {}",dto.getRealName());
    }
}
