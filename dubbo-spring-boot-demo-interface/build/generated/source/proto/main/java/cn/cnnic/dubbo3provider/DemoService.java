    package cn.cnnic.dubbo3provider;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@javax.annotation.Generated(
value = "by Dubbo generator",
comments = "Source: DemoService.proto")
public interface DemoService {
static final String JAVA_SERVICE_NAME = "cn.cnnic.dubbo3provider.DemoService";
static final String SERVICE_NAME = "demoservice.DemoService";

    // FIXME, initialize Dubbo3 stub when interface loaded, thinking of new ways doing this.
    static final boolean inited = DemoServiceDubbo.init();

    cn.cnnic.dubbo3provider.HelloReply sayHello(cn.cnnic.dubbo3provider.HelloRequest request);

    CompletableFuture<cn.cnnic.dubbo3provider.HelloReply> sayHelloAsync(cn.cnnic.dubbo3provider.HelloRequest request);


}
