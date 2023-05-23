package org.apache.dubbo.springboot.demo;

import org.apache.dubbo.common.stream.StreamObserver;

public interface StreamDataRpcApi {

    /**
     * @param userModel 入参请求对象
     * @param response 出参对应结果
     */
    void serverProcess(UserModel userModel,StreamObserver<UserDO> response);

    /**
     * 双向流
     * @param response 出参对应结果
     * @return 请求参数对象
     */
    StreamObserver<UserModel> clientAndDirectionProcess(StreamObserver<UserDO> response);

}
