# 这里主要是RabbitMQ如何使用、交换机类型、队列类型及区别以及场景
## 一、队列类型
### 1. work queue 工作队列（公平模式）
主要用于有多个消费者的时候做负载均衡，其中默认的是平均分配，要想实现公平分配（能者多劳）。必须设置以下参数，这里的配置两个都不能少，否则按照平均分配：

    #消息确认机制更改为手动
    spring.rabbitmq.listener.simple.acknowledge-mode=manual

    #预处理模式更改为每次读取1条消息,在消费者未回执确认之前,不在进行下一条消息的投送
    spring.rabbitmq.listener.simple.prefetch=1


### 2. lazy queue 惰必队列 
1. 主要用于积压数据的处理，数据持久化到硬盘，用到时才加载到内存，惰性队列和普通队列相比，只有很小的内存开销。惰性队列和持久化的消息可谓是“最佳拍档”。

2. 队列具备两种模式：default和lazy。默认的为default模式，在3.6.0的版本无需做任何变更。lazy模式即为惰性队列的模式，可以通过调用channel.queueDeclare方法的时候在参数中设置，也可以通过Policy的方式设置，如果一个队列同时使用这两种方式设置，那么Policy的方式具备更高的优先级。如果要通过声明的方式改变已有队列的模式，那么只能先删除队列，然后再重新声明一个新的。

3. 我们可以类比一下：发送1千万条消息，每条消息的大小为1KB，并且此时没有任何的消费者，那么普通队列会消耗1.2GB内存，而惰性队列只能消耗1.5MB的内存。

4. 惰性队列会将接收到的消息直接存入文件系统，而不管是持久化的或者是非持久化的，这样可以减少内存的消耗，但是会增加I/O的使用，如果消息是持久化的，那么这样的I/O操作不可避免，惰性队列和持久化的消息可谓是“最佳拍档”。注意如果惰性队列中存储的是非持久化的消息，内存的使用率会一直很稳定，但是重启之后消息一样会丢失。 


    Map<String, Object> args = new HashMap<String, Object>();

    args.put("x-queue-mode", "lazy");

    channel.queueDeclare("myqueue", false, false, false, args);


### 3. delay queue 延时队列
实现方式有两种：死信队列  、 延时插件

### 4. dead queue 死信队列

### 5. Quorum Queue 仲裁队列（注重可靠性）
quorum 队列是 RabbitMQ 的现代队列类型，基于Raft 共识算法实现持久的、复制的 FIFO 队列。它从 RabbitMQ 3.8.0 开始可用。

Quorum 队列和流取代了持久镜像队列， 仲裁队列针对数据安全是重中之重的一组用例进行了优化。仲裁队列应被视为复制队列类型的默认选项。

与经典镜像队列相比，仲裁队列在行为 和一些限制方面也有重要差异，包括特定于工作负载的队列，例如，当消费者重复重新排队相同的消息时。

某些功能（如毒消息处理）是特定于仲裁队列的。 对于受益于复制和可重复读取的情况，流可能是比仲裁队列更好的选择。

    @Bean
    public Queue quorumQueue() {
    return QueueBuilder
           .durable("quorum.queue") // 持久化
           .quorum() // 仲裁队列
           .build();
    }

[RabbitMQ高可用--Quorum Queue(仲裁队列)的用法]：（https://knife.blog.csdn.net/article/details/126740548）

## 二、 Exchange交换机类型