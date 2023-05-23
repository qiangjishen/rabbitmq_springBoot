package apache.dubbo.springboot.demo.provider.controller;

import apache.dubbo.springboot.demo.provider.RocketMQProducerService;
import cn.cnnic.dataCenter.dto.CustomerDto;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mq")
public class RockerController {

    @Autowired
    private RocketMQProducerService service;


    @GetMapping("/send/{msg}")
    public void sendMessage(@PathVariable String msg) {
        CustomerDto c = new CustomerDto();
        c.setId(Long.valueOf(1));
        c.setRealName("马斯克");
        c.setAddress("北京");
        c.setEmail("mask@twitter.com");

       // service.sendAsyncMsg(JSONObject.toJSONString(c));
        service.sendMessageInTransaction(JSONObject.toJSONString(c));
    }
    @GetMapping("/send2/{msg}")
    public void sendMessage2(@PathVariable String msg) {
        CustomerDto c = new CustomerDto();
        c.setId(Long.valueOf(1));
        c.setRealName("强斯克");
        c.setAddress("北京");
        c.setEmail("mask@twitter.com");

        // service.sendAsyncMsg(JSONObject.toJSONString(c));
        service.sendMessageInTransaction2(JSONObject.toJSONString(c));
    }
    @GetMapping("/sendTag/{msg}")
    public void sendMessageTag(@PathVariable String msg) {
        CustomerDto c = new CustomerDto();
        c.setId(Long.valueOf(1));
        c.setRealName("刘丽丽");
        c.setAddress("北京");
        c.setEmail("mask@twitter.com");

        // service.sendAsyncMsg(JSONObject.toJSONString(c));
        service.sendTagMsg(JSONObject.toJSONString(c));
    }
}
