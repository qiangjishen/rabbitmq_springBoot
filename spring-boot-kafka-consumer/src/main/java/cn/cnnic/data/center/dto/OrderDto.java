package cn.cnnic.data.center.dto;


import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Table(name = "tb_kafka_order")
public class OrderDto {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;

    private Long orderId;

    private String keyy;

    private String context;

    private LocalDateTime updateTime;

}
