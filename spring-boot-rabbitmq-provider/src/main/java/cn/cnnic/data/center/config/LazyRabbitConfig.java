package cn.cnnic.data.center.config;

import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 惰性队列,最大特点：处理消息积压，保存在硬盘
 */
@Component
@Slf4j
public class LazyRabbitConfig {
    public static final String LAZY_TOPIC_EXCHANGE = "lazy.topic.exchange";
    public static final String LAZY_TOPIC_QUEUE = "lazy.topic.queue";
    public static final String LAZY_TOPIC_ROUTING_KEY = "*.topic.*";

    /**
     * 声明队列
     */
    @Bean
    public Queue topicLazyQueue(){
        Map<String, Object> args = Maps.newHashMap();

        args.put("x-queue-mode", "lazy");
        /**
         * 设置持久化队列
         */
        return QueueBuilder.durable(LAZY_TOPIC_QUEUE).withArguments(args).build();
    }

    /**
     * 声明Topic类型交换器
     */
    @Bean
    public TopicExchange topicLazyExchange(){
        TopicExchange exchange = new TopicExchange(LAZY_TOPIC_EXCHANGE);
        return exchange;
    }

    /**
     * Topic交换器和队列通过bindingKey绑定
     * @return
     */
    @Bean
    public Binding bindingTopicLazyExchangeQueue(@Qualifier("topicLazyQueue") Queue topicLazyQueue,
                                                 @Qualifier("topicLazyExchange") TopicExchange topicLazyExchange){
        return BindingBuilder.bind(topicLazyQueue).to(topicLazyExchange).with(LAZY_TOPIC_ROUTING_KEY);
    }



}
