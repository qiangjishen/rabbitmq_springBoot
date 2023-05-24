package cn.cnnic.data.center.service.impl;

import cn.cnnic.data.center.config.CancleDelayedConfig;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CancelDelayedService {

    @RabbitListener(queues = CancleDelayedConfig.CANCLE_QUEUE_NAME, ackMode = "MANUAL")
    public void cancelDelayedMessage(Message message, Channel channel) throws Exception {
       // Connection connection = connectionFactory.createConnection();
       // Channel channel = connection.createChannel(false);
        long deliveryTag = message.getDelayTimeLevel();
        channel.basicAck(deliveryTag,true);//从队列中删除该消息。

        log.info("ok----");
        GetResponse response = channel.basicGet("delayed.queue", false);
        log.info("respnse----{}",response);
        while (response != null) {
            String id = response.getProps().getMessageId();
            log.info("收到消息--->{}",id);
            String expiration = response.getProps().getExpiration();
            if ("cnnic--0001".equals(id)) {
                log.info("=================");
                channel.basicAck(response.getEnvelope().getDeliveryTag(), true);

                break;
            }
            response = channel.basicGet("delayed.queue", false);
        }
       // channel.close();
       // connection.close();
    }
}
