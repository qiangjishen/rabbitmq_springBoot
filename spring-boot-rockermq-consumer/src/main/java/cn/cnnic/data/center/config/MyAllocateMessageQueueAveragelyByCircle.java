package cn.cnnic.data.center.config;

import org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.ArrayList;
import java.util.List;

/**
 环形平均分配: 所有Consumers 形成一个环，所有MessageQueues 依次分配到环上每个Consumer。 环形平均分配
 */
public class MyAllocateMessageQueueAveragelyByCircle implements AllocateMessageQueueStrategy {
    @Override
    public List<MessageQueue> allocate(String consumerGroup, String currentCID, List<MessageQueue> mqAll, List<String> cidAll) {
        // 这里省略校验部分，重点关注算法

        List<MessageQueue> result = new ArrayList<>();
        int index = cidAll.indexOf(currentCID);  // 当前consumer 的下标
        for (int i = index; i < mqAll.size(); i++) {
            if (i % mqAll.size() == index) {   // 下标为i 的MessageQueue 分别给 下标为i 的 consumer.
                result.add(mqAll.get(i));
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return null;
    }
}
