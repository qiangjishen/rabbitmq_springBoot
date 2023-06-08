# RocketMQ 最新版PushConsumer配置参数详解

## 1、Push消费模式下的配置
Push 默认使用的是DefaultMQPushConsumer。

## 2、consumerGroup
Consumer 组名，参数默认值是：DEFAULT_CONSUMER，多个 Consumer如果属于一个应用，订阅同样的消息，且消费逻辑一致，则应该将它们归为同一组

## 3、messageModel
CLUSTERING 消息模型，支持以下两种 1、集群消费 2、广播消费。
两种模式有哪些不同：
### (1)分配 Topic 对应消息队列的算法不同
RebalanceImpl类rebalanceByTopic 函数中 分配 Topic 对应消息队列的算法不同。

广播模式( BROADCASTING ) 下，分配 Topic 对应的所有消息队列。

集群模式( CLUSTERING ) 下，根据 队列分配策略( AllocateMessageQueueStrategy ) 分配消息队列

### (2)ACK消费机制不同
RemoteBrokerOffsetStore ：Consumer 集群模式 下，使用远程 Broker 消费进度 offset。集群模式，消费失败的消息发回到 Broker，如果发回Broker失败，就会放到Retry队列。
LocalFileOffsetStore ：Consumer 广播模式下，使用本地 文件 消费进度offset。广播模式，无论是否消费失败，不发回消息到 Broker，只打印日志。

## 4、consumeFromWhere
定义消费Client从那个位置消费消息，分别为：
CONSUME_FROM_LAST_OFFSET 默认策略，从该队列最尾开始消费，即跳过历史消息
CONSUME_FROM_FIRST_OFFSET 从队列最开始开始消费，即历史消息（还储存在broker的）全部消费一遍
CONSUME_FROM_TIMESTAMP 从某个时间点开始消费，和setConsumeTimestamp()配合使用，默认是半个小时以前

这里重点要说的就是默认配置CONSUME_FROM_LAST_OFFSET ，CONSUME_FROM_LAST_OFFSET官方的解释是一个新的订阅组第一次启动从队列的最后位置开始消费，后续再启动接着上次消费的进度开始消费，但某些情况下却并不是这样。先看下RemoteBrokerOffsetStore类中给出的消费client 跟Brocker 之间同步Offset的策略。

    @Override
    public long readOffset(final MessageQueue mq, final ReadOffsetType type) {
        if (mq != null) {
            switch (type) {
                case MEMORY_FIRST_THEN_STORE:
                case READ_FROM_MEMORY: {
                    AtomicLong offset = this.offsetTable.get(mq);
                    if (offset != null) {
                        return offset.get();
                    } else if (ReadOffsetType.READ_FROM_MEMORY == type) {
                        return -1;
                    }
                }
                case READ_FROM_STORE: {
                    try {
                        long brokerOffset = this.fetchConsumeOffsetFromBroker(mq);
                        AtomicLong offset = new AtomicLong(brokerOffset);
                        this.updateOffset(mq, offset.get(), false);
                        return brokerOffset;
                    }
                    // No offset in broker
                    catch (MQBrokerException e) {
                        return -1;
                    }
                    //Other exceptions
                    catch (Exception e) {
                        log.warn("fetchConsumeOffsetFromBroker exception, " + mq, e);
                        return -2;
                    }
                }
                default:
                    break;
            }
        }

        return -1;
    }



当Consumer客户端启动的时候无论如何是从内存中获取不到offset的，只能从远程Broker里面读了。如果消息数据从未清理过，或新添加了broker，或topic新扩容了队列，那么这几种情况可能会存在RocketMQ认为topic的队列新上线不久，数据不算太多的情形。另外，参考RocketMQ源码的注释可以理解其深意：

（1）订阅组不存在情况下，如果这个队列的消息最小Offset是0，则表示这个Topic上线时间不长，

（2）服务器堆积的数据也不多，那么这个订阅组就从0开始消费。

（3）尤其对于Topic队列数动态扩容时，必须要从0开始消费。


## 5、allocateMessageQueueStrategy
AllocateMessageQueueByConfig根据配置分配消息队列。AllocateMessageQueueAveragelyByCircle环状分配消息队列AllocateMessageQueueByMachineRoom平均分配消息队列。该平均分配方式和 AllocateMessageQueueAveragely 略有不同，其是将多余的结尾部分分配给前 rem 个 Consumer。
AllocateMessageQueueAveragely 平均分配消息队列，也是默认分配算法。
先看下分配队列需要数据结构，这些参数都不能为空

	List<MessageQueue> allocate(
        final String consumerGroup, //当前消费client处理的消息组
        final String currentCID,//当前消费client的客户端ID
        final List<MessageQueue> mqAll,//消息组包含的所有消息队列列表
        final List<String> cidAll  //消息组对应的所有消费Client的客户端ID列表
    );
    AllocateMessageQueueAveragely 类源码解析：
    
	1: public class AllocateMessageQueueAveragely implements 	AllocateMessageQueueStrategy {
	2:     private final Logger log = ClientLogger.getLog();
	3: 
	4:     @Override
	5:     public List<MessageQueue> allocate(String consumerGroup, String currentCID, List<MessageQueue> mqAll,
 	6:         List<String> cidAll) {
 	7:         // 校验参数是否正确
	8:         if (currentCID == null || currentCID.length() < 1) {
	9:             throw new IllegalArgumentException("currentCID is empty");
 	10:         }
 	11:         if (mqAll == null || mqAll.isEmpty()) {
	12:             throw new IllegalArgumentException("mqAll is null or mqAll empty");
	13:         }
 	14:         if (cidAll == null || cidAll.isEmpty()) {
	15:             throw new IllegalArgumentException("cidAll is null or cidAll empty");
	16:         }
	17: 
	18:         List<MessageQueue> result = new ArrayList<>();
	19:         if (!cidAll.contains(currentCID)) {
	20:             log.info("[BUG] ConsumerGroup: {} The consumerId: {} not in cidAll: {}",
 	21:                 consumerGroup,
	22:                 currentCID,
	23:                 cidAll);
 	24:             return result;
	25:         }
	26:         // 平均分配
	27:         int index = cidAll.indexOf(currentCID); // 第几个consumer。
 	28:         int mod = mqAll.size() % cidAll.size(); // 余数，即多少消息队列无法平均分配。
	29:         int averageSize =
	30:             mqAll.size() <= cidAll.size() ? 1 : (mod > 0 && index < mod ? mqAll.size() / cidAll.size()
	31:                 + 1 : mqAll.size() / cidAll.size());
	32:         int startIndex = (mod > 0 && index < mod) ? index * averageSize : index * averageSize + mod; // 有余数的情况下，[0, mod) 平分余数，即每consumer多分配一个节点；第index开始，跳过前mod余数。
	33:         int range = Math.min(averageSize, mqAll.size() - startIndex); // 分配队列数量。之所以要Math.min()的原因是，mqAll.size() <= cidAll.size()，部分consumer分配不到消息队列。
	34:         for (int i = 0; i < range; i++) {
	35:             result.add(mqAll.get((startIndex + i) % mqAll.size()));
	36:         }
	37:         return result;
 	38:     }
	39: 
	40:     @Override
	41:     public String getName() {
    42:         return "AVG";
 	43:     }
 	44: }

	•	说明 ：平均分配队列策略。
	•	第 7 至 25 行 ：参数校验。
	•	第 26 至 36 行 ：平均分配消息队列。
	o	第 27 行 ：index ：当前 Consumer 在消费集群里是第几个。这里就是为什	么需要对传入的 cidAll 参数必须进行排序的原因。如果不排序，Consumer 在本地计算出来的 index 无法一致，影响计算结果。
	o	第 28 行 ：mod ：余数，即多少消息队列无法平均分配。
	o	第 29 至 31 行 ：averageSize ：代码可以简化成 (mod > 0 && index < mod ? mqAll.size() / cidAll.size() + 1 : mqAll.size() / cidAll.size())。
		[ 0, mod ) ：mqAll.size() / cidAll.size() + 1。前面 mod 个 Consumer 平分余数，多获得 1 个消息队列。
		[ mod, cidAll.size() ) ：mqAll.size() / cidAll.size()。
	o	第 32 行 ：startIndex ：Consumer 分配消息队列开始位置。
	o	第 33 行 ：range ：分配队列数量。之所以要 Math#min(...) 的原因：当 mqAll.size() <= cidAll.size() 时，最后几个 Consumer 分配不到消息队列。
	o	第 34 至 36 行 ：生成分配消息队列结果。


## 6 、consumeMessageBatchMaxSize
批量消费最大消息条数，取值范围: [1, 1024]。默认是1

## 7 、pullBatchSize
消费者去broker拉取消息时，一次拉取多少条。取值范围: [1, 1024]。默认是32 。可选配置

## 8 、pullInterval
检查拉取消息的间隔时间，由于是长轮询，所以为 0，但是如果应用为了流控，也可以设置大于 0 的值，单位毫秒，取值范围: [0, 65535]

## 9、 offsetStore
集群消费：从远程Broker获取。
广播消费：从本地文件获取。
看下面源代码：

	switch (this.defaultMQPushConsumer.getMessageModel()) {
                        case BROADCASTING:
                            this.offsetStore = new LocalFileOffsetStore(this.mQClientFactory, this.defaultMQPushConsumer.getConsumerGroup());
                            break;
                        case CLUSTERING:
                            this.offsetStore = new RemoteBrokerOffsetStore(this.mQClientFactory, this.defaultMQPushConsumer.getConsumerGroup());
                            break;
                        default:
                            break;
                    }
                  this.defaultMQPushConsumer.setOffsetStore(this.offsetStore);


## 10、consumeThreadMin 和 consumeThreadMax
设置消费线程数大小取值范围都是 [1, 1000]。
4.2版本中的默认配置为：
consumeThreadMin 默认是20
consumeThreadMax 默认是64

## 11、consumeConcurrentlyMaxSpan
单队列并行消费允许的最大跨度取值范围都是 [1, 65535]，默认是2000。
这个参数只在并行消费的时候才起作用。参考 DefaultMQPushConsumerImpl 类pullMessage函数源码片段：

	if (!this.consumeOrderly) {
            if (processQueue.getMaxSpan() > this.defaultMQPushConsumer.getConsumeConcurrentlyMaxSpan()) {
                this.executePullRequestLater(pullRequest, PULL_TIME_DELAY_MILLS_WHEN_FLOW_CONTROL);
                if ((queueMaxSpanFlowControlTimes++ % 1000) == 0) {
                    log.warn(
                        "the queue's messages, span too long, so do flow control, minOffset={}, maxOffset={}, maxSpan={}, pullRequest={}, flowControlTimes={}",
                        processQueue.getMsgTreeMap().firstKey(), processQueue.getMsgTreeMap().lastKey(), processQueue.getMaxSpan(),
                        pullRequest, queueMaxSpanFlowControlTimes);
                }
                return;
            }
        }

## 12 、pullThresholdForTopic和pullThresholdSizeForTopic
pullThresholdForTopic为每个topic在本地缓存最多的消息条数,取值范围[1, 6553500]，默认的-1。
pullThresholdSizeForTopic 是在topic级别限制了消息缓存的大小，单位为Mib，取值范围[1, 102400]，默认为-1

## 13、 pullThresholdForQueue 和pullThresholdSizeForQueue
pullThresholdForQueue是拉消息本地队列缓存消息最大数，用于topic级别的流量控制，控制单位为消息个数，取值范围都是 [1, 65535]，默认是1000。如果设置了pullThresholdForTopic，就是限制了topic级别的消息缓存数（通常没有），那么会将本地每个queue的缓存数更新为pullThresholdForTopic / currentQueueCount 限制总数 / 队列数。

pullThresholdSizeForQueue 是 topic级别缓存大小限制，取值范围 [1, 1024]，默认是100Mib,如果设置了这个参数，queue的缓存大小更新为pullThresholdSizeForTopic / currentQueueCount 限制总大小 / 队列数
RebalancePushImpl类中源码片段：

	int currentQueueCount = this.processQueueTable.size();
        if (currentQueueCount != 0) {
            int pullThresholdForTopic = this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getPullThresholdForTopic();
            if (pullThresholdForTopic != -1) {
                int newVal = Math.max(1, pullThresholdForTopic / currentQueueCount);
                log.info("The pullThresholdForQueue is changed from {} to {}",
                    this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getPullThresholdForQueue(), newVal);
                this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().setPullThresholdForQueue(newVal);
            }

            int pullThresholdSizeForTopic = this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getPullThresholdSizeForTopic();
            if (pullThresholdSizeForTopic != -1) {
                int newVal = Math.max(1, pullThresholdSizeForTopic / currentQueueCount);
                log.info("The pullThresholdSizeForQueue is changed from {} to {}",
                    this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().getPullThresholdSizeForQueue(), newVal);
                this.defaultMQPushConsumerImpl.getDefaultMQPushConsumer().setPullThresholdSizeForQueue(newVal);
            }
        }

## 14、 messageListener
