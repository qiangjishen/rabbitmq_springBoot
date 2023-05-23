package apache.dubbo.springboot.demo.provider.service.impl;


import apache.dubbo.springboot.demo.provider.config.DelayQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 消费者：基于插件的延迟消息
 */
@Component
@Slf4j
public class DelayQueueConsume {
    @RabbitListener(queues = DelayQueueConfig.DELAYED_QUEUE_NAME, ackMode = "MANUAL")
    public void receiveDelayQueue(Message msg) {
        String message = new String(msg.getBody());
        log.info(msg.getMessageProperties().getMessageId());
        System.out.println("当前时间:"+new Date()+",收到延迟队列的消息:"+ message);
        //log.info("当前时间:{},收到延迟队列的消息:{}", new Date(), message);
    }
}
