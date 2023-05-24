package cn.cnnic.data.center.controller;


import cn.cnnic.data.center.config.DelayQueueConfig;
import cn.cnnic.dataCenter.dto.CustomerDto;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/ttl")
public class DelayedController {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 基于插件：生产者，发送消息和延迟时间
     */
    @GetMapping("/sendDelaymsg/{msg}/{delayTime}")
    public void sendMsg(@PathVariable("msg") String msg, @PathVariable("delayTime") Integer delayTime) {
        CustomerDto c = new CustomerDto();
        c.setId(Long.valueOf(1));
        c.setRealName("马斯克");
        c.setAddress("北京");
        c.setEmail("mask@twitter.com");

        msg = JSONObject.toJSONString(c);
         log.info("当前时间:{},发送时长为{}毫秒的信息给延迟队列队列delayed.queue:{}", new Date(), delayTime, msg);
        rabbitTemplate.convertAndSend(DelayQueueConfig.DELAYED_EXCHANGE_NAME
                , DelayQueueConfig.DELAYED_ROUTING_KEY, msg, message -> {
                    message.getMessageProperties().setMessageId("cnnic--0001");
                    //设置发送消息的延迟时间
                    message.getMessageProperties().setDelay(delayTime);
                    return message;
                });
    }

}
