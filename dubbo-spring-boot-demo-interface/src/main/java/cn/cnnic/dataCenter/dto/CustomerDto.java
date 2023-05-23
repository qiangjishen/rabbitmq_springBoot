package cn.cnnic.dataCenter.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CustomerDto implements Serializable {

    @JSONField(ordinal = 0)
    private Long id;

    @JSONField(ordinal = 1)
    private String realName;

    @JSONField(ordinal = 2)
    private String mobile;

    @JSONField(ordinal = 3)
    private String email;

    private String password;

    @JSONField(ordinal = 4)
    private String company;

    @JSONField(ordinal = 5)
    private String address;

    @JSONField(ordinal = 6)
    private int status;

    @JSONField(ordinal = 7)
    private LocalDateTime createTime;

    @JSONField(ordinal = 8)
    private LocalDateTime updateTime;
}
