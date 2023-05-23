    package org.apache.dubbo.hello;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@javax.annotation.Generated(
value = "by Dubbo generator",
comments = "Source: helloworld.proto")
public interface Greeter {
static final String JAVA_SERVICE_NAME = "org.apache.dubbo.hello.Greeter";
static final String SERVICE_NAME = "helloworld.Greeter";

    // FIXME, initialize Dubbo3 stub when interface loaded, thinking of new ways doing this.
    static final boolean inited = GreeterDubbo.init();

    org.apache.dubbo.hello.HelloReply greet(org.apache.dubbo.hello.HelloRequest request);

    CompletableFuture<org.apache.dubbo.hello.HelloReply> greetAsync(org.apache.dubbo.hello.HelloRequest request);


}
