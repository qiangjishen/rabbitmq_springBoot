package cn.cnnic.data.center.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WorkRabbitConfig {
    //队列
    public static final String WORK_QUEUE_NAME = "work.queue";
    //交换机
    public static final String WORK_EXCHANGE_NAME = "work.exchange";
    //routingKey
    public static final String WORK_ROUTING_KEY = "work.routingkey";

    //超过最大重试次数后，投递后错误队列
    public static final String ERROR_EXCHANGE_NAME = "error.exchange";

    public static final String ERROR_ROUTING_KEY = "error.routingkey";

    public static final String ERROR_QUEUE_NAME = "error.queue";

    //声明交换机
    @Bean
    public DirectExchange workExchange() {

        /**
         * 1.交换机名称
         * 2.交换机类型
         * 3.是否需要持久化
         * 4.是否需要自动删除
         * 5.其他参数
         **/
        return new DirectExchange(WORK_EXCHANGE_NAME, true, false);
    }

    //声明队列
    @Bean
    public Queue workQueue() {
        Map<String, Object> arguments = new HashMap<>();
        //指定交换机
        arguments.put("queue-mode", "lazy");

        return new Queue(WORK_QUEUE_NAME,true,false,false,arguments);
    }

    @Bean
    public Queue lazyQueue2() {
        return  QueueBuilder.durable("lazy.queue").lazy().build();
    }


    //绑定

    /**
     * 如果取消绑定，会触发ReturnBack函数
     *
     **/
    @Bean
    public Binding bindingWorkQueue(@Qualifier("workQueue") Queue workQueue,
                                       @Qualifier("workExchange") DirectExchange workExchange) {

        return BindingBuilder.bind(workQueue).to(workExchange).with(WORK_ROUTING_KEY);
    }

    /**
     * 定义 MessageRecoverer 将错误消息发送到指定队列
     */
    @Bean
    public MessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate){
        return new RepublishMessageRecoverer(rabbitTemplate, ERROR_EXCHANGE_NAME, ERROR_ROUTING_KEY);
    }


}
