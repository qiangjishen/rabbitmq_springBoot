package apache.dubbo.springboot.demo.provider.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfirmCallbackService implements RabbitTemplate.ConfirmCallback {

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        //发送消息时没传递correlationData，这里就为null
        log.info("消息附带标识：" + correlationData);
        //消息是否到达broker
        log.info("消息是否到达broker：" + ack);
        //消息发送失败的原因(RabbitMQ服务宕机等)
        log.info("失败原因："+cause);

        if (!ack) {
            //消息发送失败,写入到数据库，等待后续处理
            log.error("消息发送异常!");
        }else {
           // log.info("发送者爸爸已经收到确认，correlationData={} ,ack={}, cause={}", correlationData.getId(), ack, cause);
            log.info("发送者爸爸已经收到确认，correlationData={} ,ack={}, cause={}", correlationData, ack, cause);
        }
    }
}
