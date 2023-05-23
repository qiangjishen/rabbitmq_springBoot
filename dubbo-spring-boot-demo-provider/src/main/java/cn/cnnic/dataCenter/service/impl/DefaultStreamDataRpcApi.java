package cn.cnnic.dataCenter.service.impl;

import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.springboot.demo.StreamDataRpcApi;
import org.apache.dubbo.springboot.demo.UserDO;
import org.apache.dubbo.springboot.demo.UserModel;

@DubboService
public class DefaultStreamDataRpcApi implements StreamDataRpcApi {
    /**
     * 服务端流
     * @param userModel 入参请求对象
     * @param response 出参对应结果
     */
    @Override
    public void serverProcess(UserModel userModel, StreamObserver<UserDO> response) {
        UserDO userDO = new UserDO();
        userDO.setName(userModel.getName());
        userDO.setAge(userModel.getAge());
        userDO.setId(userModel.getId());
        response.onNext(userDO);

        userDO.setName("ffffffffffuck");
        response.onNext(userDO);

        response.onCompleted();
    }


    @Override
    public StreamObserver<UserModel> clientAndDirectionProcess(StreamObserver<UserDO> response) {
        return new StreamObserver<UserModel>(){
            @Override
            public void onNext(UserModel userModel) {
                System.out.println("server------"+userModel.getName());
                UserDO userDO = new UserDO();
                userDO.setName(userModel.getName());
                userDO.setAge(userModel.getAge());
                userDO.setId(userModel.getId());
                response.onNext(userDO);

                UserDO userDO2 = new UserDO();
                userDO2.setName("marry...");
                userDO2.setAge(60);
                userDO2.setId(userModel.getId());
                response.onNext(userDO2);


            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable);
            }

            @Override
            public void onCompleted() {
                response.onCompleted();
                System.out.println("completed");
            }
        };
    }
}
