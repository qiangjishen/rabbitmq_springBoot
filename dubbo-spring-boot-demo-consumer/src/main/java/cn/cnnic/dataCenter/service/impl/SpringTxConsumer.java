package cn.cnnic.dataCenter.service.impl;

import cn.cnnic.dataCenter.dto.CustomerDto;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "CNNIC_ROCKETMQ_TOPIC1",
        consumerGroup = "transaction-consumer-group",
        selectorExpression = "tag1")
public class SpringTxConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String msg) {
        log.info("接收到消息 -> {}", msg);
        CustomerDto dto = JSONObject.parseObject(msg, CustomerDto.class);
        log.info(dto.getRealName());
    }
}

