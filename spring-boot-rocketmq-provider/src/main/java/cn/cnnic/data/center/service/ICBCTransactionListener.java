package cn.cnnic.data.center.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/*
 * 【事务监听器（本地事务）】模拟工行进行扣款活动
 */
@Slf4j
@RocketMQTransactionListener()
public class ICBCTransactionListener implements RocketMQLocalTransactionListener {
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {

        MessageHeaders messageHeaders = msg.getHeaders();
        String transactionId = (String) messageHeaders.get(RocketMQHeaders.TRANSACTION_ID);
        log.info("预提交消息成功：{}",msg);
        log.info("【执行本地事务】消息体参数：transactionId={}", transactionId);

        try {
            StringBuilder money = new StringBuilder();
            byte[] bytes = ((byte[])msg.getPayload());
            for (int i = 0; i < bytes.length; i++) {
                money.append(bytes[i] - '0');
            }
            log.info("【执行本地事务成功】工行账户扣除" + money +"元!");
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }

    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        MessageHeaders headers = msg.getHeaders();
        String transactionId = headers.get(RocketMQHeaders.TRANSACTION_ID, String.class);
        log.info("执行消息回查:{}",msg);
        log.info("【回查本地事务】transactionId={}",transactionId);

        // 执行相关业务

        // if(...) {
        //  return RocketMQLocalTransactionState.ROLLBACK;
        // else {
        return RocketMQLocalTransactionState.COMMIT;
        // }
        // return RocketMQLocalTransactionState.UNKNOW;
    }
}

