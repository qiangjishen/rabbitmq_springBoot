package apache.dubbo.springboot.demo.provider.controller;


import apache.dubbo.springboot.demo.provider.config.CancleDelayedConfig;
import apache.dubbo.springboot.demo.provider.config.WorkRabbitConfig;
import apache.dubbo.springboot.demo.provider.service.impl.ConfirmCallbackService;
import apache.dubbo.springboot.demo.provider.service.impl.ReturnCallbackService;
import cn.cnnic.dataCenter.dto.CustomerDto;
import com.alibaba.fastjson.JSONObject;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/work")
public class WorkQueueController {


    @Autowired
    RabbitTemplate rabbitTemplate;  //使用RabbitTemplate,这提供了接收/发送等等方法

    @Autowired
    private ConfirmCallbackService confirmCallbackService;

    @Autowired
    private ReturnCallbackService returnCallbackService;



    @GetMapping("/send")
    public String sendCanalDirectMessage() {

        /**
         * 确保消息发送失败后可以重新返回到队列中
         * 注意：yml需要配置 publisher-returns: true
         */
        rabbitTemplate.setMandatory(true);


        /**
         * 消费者确认收到消息后，手动ack回执回调处理
         */
        rabbitTemplate.setConfirmCallback(confirmCallbackService);

        /**
         * 消息投递到队列失败回调处理
         */
        rabbitTemplate.setReturnCallback(returnCallbackService);

        for (int i=0; i < 10; i++) {

            String messageId = String.valueOf(UUID.randomUUID());
            String messageData = "test message, hello!-----"+i;
            String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Map<String,Object> map=new HashMap<>();
            map.put("messageId",messageId);
            map.put("messageData",messageData);
            map.put("createTime",createTime);

            CustomerDto c = new CustomerDto();
            c.setId(Long.valueOf(i));
            c.setRealName("马斯克"+i);
            c.setAddress("北京");
            c.setEmail("mask@twitter.com");

            org.apache.rocketmq.common.message.Message mm = new org.apache.rocketmq.common.message.Message();
            mm.setBody(JSONObject.toJSONString(c).getBytes());



            //将消息携带绑定键值：TestDirectRouting 发送到交换机TestDirectExchange
            //rabbitTemplate.convertAndSend(WorkRabbitConfig.WORK_EXCHANGE_NAME, WorkRabbitConfig.WORK_ROUTING_KEY, mm);
            // rabbitTemplate.convertAndSend(WorkRabbitConfig.WORK_QUEUE_NAME,mm);

            MessageProperties properties  = new MessageProperties();
            /**
             * 设置消息唯一标识
             */
            properties.setMessageId(UUID.randomUUID().toString());

            /**
             * 创建消息包装对象
             */
            Message msg = MessageBuilder.withBody(mm.getBody()).andProperties(properties).build();

            /**
             * 相关数据
             */
            CorrelationData correlationData = new CorrelationData();
            /**
             * 消息ID，全局唯一
             */
            correlationData.setId(msg.getMessageProperties().getMessageId());

            /**
             * 发送消息
             */
            rabbitTemplate.convertAndSend(WorkRabbitConfig.WORK_EXCHANGE_NAME, WorkRabbitConfig.WORK_ROUTING_KEY, mm,
                    message -> {
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);

                        return message;
                    },
                    correlationData);

        }
        return "ok";
    }

    @GetMapping("/cancle/{messageId}/{delayTime}")
    public String cancleDelayedMessage(@PathVariable String messageId, @PathVariable int delayTime) {
        CustomerDto c = new CustomerDto();
        c.setId(Long.valueOf(1));
        c.setRealName("马mmmmmm");
        c.setAddress("北京");
        c.setEmail("mask@twitter.com");

        String messageIds = "cnnic--0001";

        org.apache.rocketmq.common.message.Message mm = new org.apache.rocketmq.common.message.Message();
        mm.setBody(JSONObject.toJSONString(c).getBytes());
        rabbitTemplate.convertAndSend(CancleDelayedConfig.CANCLE_EXCHANGE_NAME, CancleDelayedConfig.CANCLE_ROUTING_KEY, mm, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration(delayTime + "");
                message.getMessageProperties().setMessageId(messageIds);
                return message;

            }
        });
        log.info("取消发送成功......................");

        return "ok";
    }
}
