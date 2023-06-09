package cn.cnnic.data.center.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderDto {
    private String id;

    private String operType;

    private String msg;

    private LocalDateTime updateTime;
}
