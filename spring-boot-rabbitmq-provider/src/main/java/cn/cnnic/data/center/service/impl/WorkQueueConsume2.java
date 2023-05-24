package cn.cnnic.data.center.service.impl;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class WorkQueueConsume2 {
    /**
     * 注意：提交模式为： 手动提交
     * @param message
     * @param channel
     * @throws InterruptedException
     * @throws IOException
     */
    @RabbitListener(queues = "work.queue", concurrency = "1", ackMode = "MANUAL")
    public void process(Message message, Channel channel) throws InterruptedException, IOException {
        Thread.sleep(5000);

        //log.info(""+1/0);
        long deliveryTag = message.getDelayTimeLevel();
        byte[] body = message.getBody();
        String msg = new String(body);
        log.info("2号消费者---->{}",msg);
        try{
            //手动创建异常
            //int c=10/0;
            //System.out.println("处理业务逻辑");
            //消费端手动确认消息
            //long deliveryTag, 表示的标识。
            // boolean multiple:是否允许多确认
            channel.basicAck(deliveryTag,true);//从队列中删除该消息。
        }catch (Exception e){
            //(long deliveryTag, boolean multiple, boolean requeue: 是否让队列再次发送该消息。
            channel.basicNack(deliveryTag,true,true);
        }
    }
}
