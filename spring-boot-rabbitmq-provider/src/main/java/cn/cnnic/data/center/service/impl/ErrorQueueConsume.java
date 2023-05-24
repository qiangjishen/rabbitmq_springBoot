package cn.cnnic.data.center.service.impl;


import cn.cnnic.data.center.config.WorkRabbitConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ErrorQueueConsume {

    @RabbitListener(queues = WorkRabbitConfig.ERROR_QUEUE_NAME, concurrency = "1", ackMode = "MANUAL")
    public void process(Message message, Channel channel) throws InterruptedException, IOException, Exception {
        //Thread.sleep(1000);

        //获取信息的标识
        long deliveryTag = message.getDelayTimeLevel();

        byte[] body = message.getBody();
        String msg = new String(body);
        log.info("超过重试次数，人工干预---->{}", msg);

        //手动创建异常
        //int c=10/0;
        //System.out.println("处理业务逻辑");
        //消费端手动确认消息
        //long deliveryTag, 表示的标识。
        // boolean multiple:是否允许多确认
        //确认消息
        channel.basicAck(deliveryTag, true);//从队列中删除该消息。



    }
}
