package apache.dubbo.springboot.demo.provider.service.impl;

import cn.cnnic.dataCenter.dto.CustomerDto;
import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DefaultMQProducer1 {
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("producer_A");
        // NameServer
        producer.setNamesrvAddr("localhost:9876");
        // 指定异步发送失败后不进行重试发送
        producer.setRetryTimesWhenSendAsyncFailed(0);
        // 指定新创建的Topic的Queue数量为2，默认为4
        producer.setCreateTopicKey("topic_A");
        producer.setDefaultTopicQueueNums(10);
        // 开启生产者
        producer.start();
        // 模拟发送100条消息
        for (int i = 0; i < 10; i++) {
            byte[] bytes = ("hi " + i).getBytes();
            CustomerDto c = new CustomerDto();
            c.setId(Long.valueOf(i));
            c.setRealName("马斯克"+i);
            c.setAddress("北京");
            c.setEmail("mask@twitter.com");


            Message msg = new Message("topic_A", "*", JSONObject.toJSONString(c).getBytes());
            /**
             try {
                // 异步发送，指定回调
                producer.send(msg, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        System.out.println(sendResult);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
**/
            int orderId = 0;
            producer.send(msg, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    Integer id = (Integer) arg;
                    int index = id % mqs.size();
                    System.out.println("id="+id+", index="+index+", mqs.size="+mqs.size());
                    return mqs.get(index);
                }
            }, orderId);

        }
        // 异步发送，如果不执行sleep，则消息在发送之前，producer已经关闭，就会报错
        TimeUnit.SECONDS.sleep(3);
        // 关闭生产者
        producer.shutdown();
    }

}
