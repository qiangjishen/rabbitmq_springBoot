package cn.cnnic.dataCenter.service.impl;


import cn.cnnic.dataCenter.dao.CustomerDao;
import cn.cnnic.dataCenter.dto.CustomerDto;
import cn.cnnic.dataCenter.service.CustomerService;
import com.alibaba.fastjson.JSONObject;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.springboot.demo.UserDO;
import org.apache.dubbo.springboot.demo.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@DubboService
@Component
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerDao customerDao;

    @Override
    public List<CustomerDto> list(JSONObject jsonObject) {

        List<CustomerDto> list = customerDao.list(jsonObject);
        return list;
    }

    @Override
    public void serverProcess(JSONObject jsonObject,StreamObserver<String> response) {
        List<CustomerDto> list = customerDao.list(jsonObject);

        response.onNext(JSONObject.toJSONString(JSONObject.toJSON(list)));
    }


}
