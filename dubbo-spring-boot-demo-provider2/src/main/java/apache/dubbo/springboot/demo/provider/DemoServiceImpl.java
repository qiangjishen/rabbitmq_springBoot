package apache.dubbo.springboot.demo.provider;

import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.springboot.demo.DemoService2;

@DubboService
public class DemoServiceImpl implements DemoService2 {
    @Override
    public String sayHello(String name) {
        System.out.println("-------------------222--------------------");
        return "Hello222 " + name;
    }

    @Override
    public void providerServerStream(String name, StreamObserver<Object> response) {
        // 处理name
        response.onNext("服务器端流式RPC222，第一次响应：" + name);
        response.onNext("服务器端流式RPC222，第二次响应：" + name);
        response.onCompleted();
    }
}
