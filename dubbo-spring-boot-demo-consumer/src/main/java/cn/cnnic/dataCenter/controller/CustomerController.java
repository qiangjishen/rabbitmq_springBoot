package cn.cnnic.dataCenter.controller;


import cn.cnnic.dataCenter.dto.CustomerDto;
import cn.cnnic.dataCenter.service.CustomerService;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户管理
 * <p>
 * 管理员使用
 */
@Slf4j
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @DubboReference
    private CustomerService userService;


    @GetMapping("/list/{email}")
    public List<CustomerDto> getCustomers(@PathVariable String email) {
        JSONObject jsonObject = new JSONObject();

        if (email != null) {
            jsonObject.put("email", email);
        }


        List<CustomerDto> users =  userService.list(jsonObject);

        return users;
    }


    @RequestMapping("list/stream/{email}")
    public List<CustomerDto> getStream(@PathVariable String email){
        JSONObject jsonObject = new JSONObject();
        List<CustomerDto> list = new ArrayList<>();
        String json;

       // if (email != null) {
        //    jsonObject.put("email", email);
      //  }
        userService.serverProcess(jsonObject, new StreamObserver<String>() {
            @Override
            public void onNext(String data) {
                log.info(data);


                //list = JSONObject.parseArray(data,CustomerDto.class);
                log.warn("list size-->"+list.size());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onCompleted() {

            }
        });
        return null;
    }




}
