package cn.cnnic.data.center.config;



import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class RabbitMQConfig {

    /**
    @Bean
    @Scope("prototype")
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        //开启 发送者确认
        //template.setConfirmCallback(new ConfirmCallbackService());
        //开启mandatory模式（开启失败回调）
        template.setMandatory(true);
        //template.setReturnCallback(new EmailReturnCallback());

        return template;
    }

    @Bean
    @Scope("prototype")
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        //消息手动确认
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 开启手动 ack
        //factory.setAcknowledgeMode(AcknowledgeMode.NONE); // 开启手动 ack
        //消息预取数量
        factory.setPrefetchCount(1);
        //开启 发送者确认





        return factory;
    }

     **/

    @Bean
    public Queue topicQueue1(){
        return new Queue("topicQueue1",true);
    }

    @Bean
    public Queue topicQueue2(){
        return new Queue("topicQueue2",true);
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange("topicExchange");
    }

    @Bean
    public Binding topicBinding1(){
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("ss007.id.*");
    }

    @Bean
    public Binding topicBinding2(){
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("ss007.name.*");
    }


}
