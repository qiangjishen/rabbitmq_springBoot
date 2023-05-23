package org.apache.dubbo.springboot.demo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserModel implements Serializable {
    private Long id;
    private String name;
    private Integer age;
}
