package apache.dubbo.springboot.demo.provider.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CancleDelayedConfig {
    //队列
    public static final String CANCLE_QUEUE_NAME = "cancle.queue";
    //交换机
    public static final String CANCLE_EXCHANGE_NAME = "cancle.exchange";
    //routingKey
    public static final String CANCLE_ROUTING_KEY = "cancle.routingkey";

    //声明交换机
    @Bean
    public DirectExchange cancleExchange() {

        /**
         * 1.交换机名称
         * 2.交换机类型
         * 3.是否需要持久化
         * 4.是否需要自动删除
         * 5.其他参数
         **/
        return new DirectExchange(CANCLE_EXCHANGE_NAME,
                true, false);
    }

    //声明队列
    @Bean
    public Queue cancleQueue() {
        Map<String, Object> arguments = new HashMap<>();
        //指定交换机
       // arguments.put("queue-mode", "lazy");

        return new Queue(CANCLE_QUEUE_NAME,true,false,false,arguments);
    }

    @Bean
    public Queue lazyQueue1() {
       // return  QueueBuilder.durable("lazy.queue").lazy().build();
        return new Queue(CANCLE_QUEUE_NAME,true);
    }


    //绑定
    @Bean
    public Binding bindingCancleQueue(@Qualifier("cancleQueue") Queue workQueue,
                                       @Qualifier("cancleExchange") DirectExchange workExchange) {

        return BindingBuilder.bind(workQueue).to(workExchange).with(CANCLE_ROUTING_KEY);
    }
}
