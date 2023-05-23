    package org.apache.dubbo.hello;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@javax.annotation.Generated(
value = "by Dubbo generator",
comments = "Source: helloworld.proto")
public final class GreeterDubbo {
private static final AtomicBoolean registered = new AtomicBoolean();

public static boolean init() {
    if (registered.compareAndSet(false, true)) {
            org.apache.dubbo.common.serialize.protobuf.support.ProtobufUtils.marshaller(
            org.apache.dubbo.hello.HelloRequest.getDefaultInstance());
            org.apache.dubbo.common.serialize.protobuf.support.ProtobufUtils.marshaller(
            org.apache.dubbo.hello.HelloReply.getDefaultInstance());
    }
    return true;
}

private GreeterDubbo() {}

}
