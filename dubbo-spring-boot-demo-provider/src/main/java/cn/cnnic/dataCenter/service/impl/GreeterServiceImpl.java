package cn.cnnic.dataCenter.service.impl;

import cn.cnnic.dubbo3provider.DemoService;
import cn.cnnic.dubbo3provider.HelloReply;
import cn.cnnic.dubbo3provider.HelloRequest;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;

import java.util.concurrent.CompletableFuture;

@DubboService
public class GreeterServiceImpl implements DemoService {

    @Override
    public HelloReply sayHello(HelloRequest request) {

        return HelloReply.newBuilder().setName("Hello " + request.getName()).setAge(Long.valueOf(30)).setAddress("beijing" + ", response from provider: " + RpcContext.getContext().getLocalAddress()).build();

        //
    }

    @Override
    public CompletableFuture<HelloReply> sayHelloAsync(HelloRequest request) {
        return CompletableFuture.completedFuture(sayHello(request));
    }
}
