package cn.cnnic.dataCenter.service;

import cn.cnnic.dataCenter.dto.CustomerDto;
import com.alibaba.fastjson.JSONObject;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.springboot.demo.UserDO;
import org.apache.dubbo.springboot.demo.UserModel;

import java.util.List;

public interface CustomerService {
    /**
     * 文章列表
     */
    List<CustomerDto> list(JSONObject jsonObject);


    void serverProcess(JSONObject jsonObject,  StreamObserver<String> response);
}
