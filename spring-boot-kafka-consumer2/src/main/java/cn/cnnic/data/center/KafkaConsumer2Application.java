package cn.cnnic.data.center;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class KafkaConsumer2Application {
    public static void main(String[] args) {
        SpringApplication.run(KafkaConsumer2Application.class, args);
    }
}