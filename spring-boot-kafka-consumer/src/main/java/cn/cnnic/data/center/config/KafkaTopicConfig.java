package cn.cnnic.data.center.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * topic 主题配置
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic createTopic_kafka1(){
        return new NewTopic("kafka1",4,(short) -1);
    }

    @Bean
    public NewTopic createTopic_kafka_topic_001(){
        return new NewTopic("kafka_topic_001",4,(short) -1);
    }

    @Bean
    public NewTopic createTopic_kafka_89757(){
        return new NewTopic("kafka_89757",4,(short) -1);
    }

    @Bean
    public NewTopic createTopic_plc1(){
        return new NewTopic("plc1",4,(short) -1);
    }
}

