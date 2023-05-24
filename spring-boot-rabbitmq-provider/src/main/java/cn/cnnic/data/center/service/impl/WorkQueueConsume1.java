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
public class WorkQueueConsume1 {
    //重试次数
    private int retryCount = 0;

    /**
     * 只有在 ackMode = "AUTO"模式下
     * 才会触发，超过重试次数，发往指定的交换机
     *
     * @param message
     * @param channel
     * @throws InterruptedException
     * @throws IOException
     * @throws Exception
     */
    @RabbitListener(queues = WorkRabbitConfig.WORK_QUEUE_NAME, concurrency = "1", ackMode = "MANUAL")
    public void process(Message message, Channel channel) throws InterruptedException, IOException, Exception {
        Thread.sleep(1000);

        //获取信息的标识
        long deliveryTag = message.getDelayTimeLevel();

        try {
            //int a = 1 / 0;
           // System.out.println("1号consumer消费成功  : " + message);

            byte[] body = message.getBody();
            String msg = new String(body);
            log.info("1号消费者---->{}", msg);

            //手动创建异常
            //int c=10/0;
            //System.out.println("处理业务逻辑");
            //消费端手动确认消息
            //long deliveryTag, 表示的标识。
            // boolean multiple:是否允许多确认
            //确认消息
            channel.basicAck(deliveryTag, true);//从队列中删除该消息。

        } catch (Exception e) {
            //(long deliveryTag, boolean multiple, boolean requeue: 是否让队列再次发送该消息。
            // channel.basicNack(deliveryTag,true,true);
            retryCount++;
            log.info("触发重试。。。。。。。。。。。。。。。。。");
            //重新抛出异常  触发重试机制
            throw e;

        } finally {
           // log.info("finally................"+retryCount);
            //重试次数达到限制
            /**
            if (retryCount == 3) {
                log.info("达到重试次数：---->"+retryCount);
                log.info("处理订单消息异常，nack消息到死信队列");
                //不重新入队，发送到死信队列
                channel.basicNack(deliveryTag, false, false);
            }
             **/

        }
    }

}
