package apache.dubbo.springboot.demo.provider.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * 发送方根据本地事务执行结果向 MQ Server 提交二次确认（Commit 或是 Rollback）
 */
@RocketMQTransactionListener( corePoolSize = 2, maximumPoolSize = 10)
@Slf4j
public class RocketMQTransactionListenerImpl implements RocketMQLocalTransactionListener {

    private static Map<String, RocketMQLocalTransactionState> STATE_MAP = new HashMap<>();


    /**
     *  执行业务逻辑
     *   todo 数据库相关逻辑  报错事务回滚 否则事务提交
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        String transId = (String) message.getHeaders().get(RocketMQHeaders.TRANSACTION_ID);
        try {
            //3.执行本地事务

            System.out.println("用户A账户减500元.");
            System.out.println("用户B账户加500元.");
            STATE_MAP.put(transId, RocketMQLocalTransactionState.COMMIT);
            //System.out.println(1/0);
            //4.发送方根据本地事务执行结果向 MQ Server 提交二次确认（Commit 或是 Rollback）
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            e.printStackTrace();
        }

        STATE_MAP.put(transId, RocketMQLocalTransactionState.ROLLBACK);
        return RocketMQLocalTransactionState.UNKNOWN;

    }

    /**
     * 回查
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        String transId = (String) message.getHeaders().get(RocketMQHeaders.TRANSACTION_ID);
        log.info("回查消息 -> transId ={} , state = {}", transId, STATE_MAP.get(transId));
        //return STATE_MAP.get(transId);
        return RocketMQLocalTransactionState.UNKNOWN;
    }
}
