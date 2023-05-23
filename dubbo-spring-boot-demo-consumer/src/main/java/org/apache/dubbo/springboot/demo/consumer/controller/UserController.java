package org.apache.dubbo.springboot.demo.consumer.controller;

import cn.cnnic.dubbo3provider.HelloReply;
import cn.cnnic.dubbo3provider.HelloRequest;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.springboot.demo.DemoService2;
import org.apache.dubbo.springboot.demo.StreamDataRpcApi;
import org.apache.dubbo.springboot.demo.UserDO;
import org.apache.dubbo.springboot.demo.UserModel;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    // 消费者，需要调用注册中心的服务
    // @DubboReference：注入分布式中,远程服务对象
    @DubboReference
    private DemoService2 userService;

    @DubboReference
    private StreamDataRpcApi streamDataRpcApi;

    @DubboReference
    private cn.cnnic.dubbo3provider.DemoService demoService;


    @RequestMapping("/user/name/{name}")
    public String getUser(@PathVariable("name") String name) {
        return userService.sayHello(name);
    }

    // SERVER_STREAM
    @RequestMapping ("/user/stream")
    public String serverUser() {
        return "你好，" + consumerServerStream();
    }


    @RequestMapping("user/pro/{name}")
    public String demo(@PathVariable("name") String name) {
        HelloRequest helloRequest = HelloRequest.newBuilder().setName(name).build();
        HelloReply helloReply = demoService.sayHello(helloRequest);

        return helloReply.getName()+"_"+helloReply.getAge()+"_"+helloReply.getAddress();
    }

    @RequestMapping("user/dubboStream")
    public  String streamDataApi(){
        UserModel model = new UserModel();
        model.setName("jessenqiang");
        model.setId(Long.valueOf(111));
        model.setAge(30);
        streamDataRpcApi.serverProcess(model, new StreamObserver<UserDO>() {
            @Override
            public void onNext(UserDO data) {
                System.out.println("【单】消费者接受到的：" + data.getName());
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        });
        return "complete";
    }



    @RequestMapping("user/dubboStream2")
    public  String stream2DataApi(){
        UserModel model = new UserModel();
        model.setName("jessenqiang");
        model.setId(Long.valueOf(111));
        model.setAge(30);
        StreamObserver<UserModel> result = streamDataRpcApi.clientAndDirectionProcess(new StreamObserver<UserDO>() {
            @Override
            public void onNext(UserDO data) {
                System.out.println("client---->"+data.getName());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("client---complete");
            }
        });

        result.onNext(model);
        result.onCompleted();

        return "complete";
    }


    public String consumerServerStream() {
        userService.providerServerStream("服务端流的橙子宝宝", new StreamObserver<Object>() {

            @Override
            public void onNext(Object data) {
                System.out.println("【信息】消费者接受到的：" + data);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("【错误】消费者接受到的：" + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("【完成】");
            }
        });
        return "【SERVER_STREAM】快去看看console有没有print出来啥";

    }


}

