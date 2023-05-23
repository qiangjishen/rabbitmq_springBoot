package org.apache.dubbo.springboot.demo;

import org.apache.dubbo.common.stream.StreamObserver;

public interface DemoService2 {
    String sayHello(String name);

    /**
     * SERVER_STREAM（服务器端流式 RPC）
     * 客户端发送请求到服务器，拿到一个流去读取返回的消息序列。 客户端读取返回的流，直到里面没有任何消息
     * @param name
     * @param response 通过它来返回数据
     */
    void providerServerStream(String name, StreamObserver<Object> response);
}
