package cn.cnnic.data.center.dto;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Table(name = "tb_kafka_offset")
public class OffsetDto {

    @Id
    @GeneratedValue(generator = "JDBC")
    private  Long id;

    private String groupId;

    private String topic;

    private Integer partitions;

    private Long offset;

    private LocalDateTime updateTime;
}
