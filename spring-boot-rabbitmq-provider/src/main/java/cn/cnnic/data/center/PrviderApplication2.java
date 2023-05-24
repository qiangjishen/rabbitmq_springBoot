package cn.cnnic.data.center;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class PrviderApplication2 {
    public static void main(String[] args) {
        SpringApplication.run(PrviderApplication2.class, args);
    }
}
