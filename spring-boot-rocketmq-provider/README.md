 发送消息时，根据实际情况进行选择
 
## 1. SelectMessageQueueByHash：

    public class SelectMessageQueueByHash implements MessageQueueSelector {
    /**
    mqs 是MQ里面所有的队列
    msg是消息
    arg是  producer.send 方法第三个参数
    */
    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        // 获取参数的hashcode值, 
        int value = arg.hashCode();
        
        // 防止出现负数,因为hashcode算出来的结果可能是负数，取个绝对值，这也是我们平时开发中需要注意到的点
        if (value < 0) {
            value = Math.abs(value);
        }
        // 直接取余队列个数。
        value = value % mqs.size();
        //根据余数获取队列,这个队列就是消息要投递的队列
        return mqs.get(value);
    }
    }

## 2.SelectMessageQueueByRandom 随机返回队列

## 3.SelectMessageQueueByMachineRoom 计算算法，这个算法没有实现

