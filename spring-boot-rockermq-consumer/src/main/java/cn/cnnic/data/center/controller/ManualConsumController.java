package cn.cnnic.data.center.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragelyByCircle;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;


public class ManualConsumController {

    private static final Map<MessageQueue,Long> OFFSE_TABLE = new HashMap<MessageQueue,Long>();

    public void consume() throws MQClientException {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("groupName");
        consumer.setNamesrvAddr("name-serverl-ip:9876;name-server2-ip:9876");
        //consumer.setAllocateMessageQueueStrategy(AllocateMessageQueueStrategy.class);

        consumer.start();
        // 从指定topic中拉取所有消息队列
        Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues("order-topic");
        for(MessageQueue mq:mqs){
            try {
                // 获取消息的offset，指定从store中获取
                long offset = consumer.fetchConsumeOffset(mq,true);

                System.out.println("consumer from the queue:"+mq+":"+offset);
                while(true){
                    PullResult pullResult = consumer.pullBlockIfNotFound(mq, null, getMessageQueueOffset(mq), 32);
                    putMessageQueueOffset(mq,pullResult.getNextBeginOffset());
                    switch(pullResult.getPullStatus()){
                        case FOUND:
                            List<MessageExt> messageExtList = pullResult.getMsgFoundList();
                            for (MessageExt m : messageExtList) {
                                System.out.println(new String(m.getBody()));
                            }
                            break;
                        case NO_MATCHED_MSG:
                            break;
                        case NO_NEW_MSG:
                            break;
                        case OFFSET_ILLEGAL:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        consumer.shutdown();
    }

    // 保存上次消费的消息下标
    private static void putMessageQueueOffset(MessageQueue mq,
                                              long nextBeginOffset) {
        OFFSE_TABLE.put(mq, nextBeginOffset);
    }

    // 获取上次消费的消息的下标
    private static Long getMessageQueueOffset(MessageQueue mq) {
        Long offset = OFFSE_TABLE.get(mq);
        if(offset != null){
            return offset;
        }
        return 0l;
    }




    /**
     * 推模式
     *
     * @param args
     * @author Jamin
     * @date 2021/8/16 10:13
     */
	public static void main(String[] args) throws MQClientException {
		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group-cnnic-consum1");
		consumer.setNamesrvAddr("127.0.0.1:9876");
		consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        //设置消费线程数大小取值范围都是 [1, 1000]。
        //consumeThreadMin 默认是20     consumeThreadMax 默认是64
        consumer.setConsumeThreadMin(1);
        consumer.setConsumeThreadMax(4);

        //批量消费最大消息条数，取值范围: [1, 1024]。默认是1
        consumer.setConsumeMessageBatchMaxSize(2);

        //pullThresholdForTopic为每个topic在本地缓存最多的消息条数,取值范围[1, 6553500]，默认的-1
        consumer.setPullThresholdForTopic(1000);

        //pullThresholdSizeForTopic 是在topic级别限制了消息缓存的大小，单位为Mib，取值范围[1, 102400]，默认为-1
        consumer.setPullThresholdSizeForTopic(1000);

        //是拉消息本地队列缓存消息最大数 用于topic级别的流量控制，控制单位为消息个数，
        // 取值范围都是 [1, 65535]，默认是1000
        consumer.setPullThresholdForQueue(100);

        //是 topic级别缓存大小限制，取值范围 [1, 1024]，默认是100Mib,
        consumer.setPullThresholdSizeForQueue(100);

        //检查拉取消息的间隔时间，由于是长轮询，所以为 0，但是如果应用为了流控，也可以设置大于 0 的值，单位毫秒，取值范围: [0, 65535]
        consumer.setPullInterval(2000); //拉取消息的时间间隔

        //消费者去broker拉取消息时，一次拉取多少条。取值范围: [1, 1024]。默认是32 。可选配置
        consumer.setPullBatchSize(10); //去broker拉取消息数


		consumer.subscribe("java-topic", "*");
		//如何分配消息队列给客户端，包括 AllocateMessageQueueByConfig根据配置分配消息队列、AllocateMessageQueueAveragelyByCircle环状分配消息队列、
        //AllocateMessageQueueByMachineRoom平均分配消息队列、AllocateMessageQueueAveragely 平均分配消息队列，也是默认分配算法。
		consumer.setAllocateMessageQueueStrategy(new AllocateMessageQueueAveragelyByCircle());
		consumer.registerMessageListener(new MessageListenerConcurrently() {
			@Override
			public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
				System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), list);
                MessageQueue queue = consumeConcurrentlyContext.getMessageQueue();
                int index = queue.getQueueId();

				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			}
		});
		consumer.start();

	}

}
