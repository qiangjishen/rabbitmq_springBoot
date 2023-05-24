# 这里主要是RabbitMQ如何使用、交换机类型、队列类型及区别以及场景
## 一、队列类型
### 1. work queue 工作队列（公平模式）
主要用于有多个消费者的时候做负载均衡，其中默认的是平均分配，要想实现公平分配（能者多劳）。必须设置以下参数，这里的配置两个都不能少，否则按照平均分配：

    #消息确认机制更改为手动
    spring.rabbitmq.listener.simple.acknowledge-mode=manual

    #预处理模式更改为每次读取1条消息,在消费者未回执确认之前,不在进行下一条消息的投送
    spring.rabbitmq.listener.simple.prefetch=1


### 2. lazy queue 惰必队列 
主要用于积压数据的处理，数据持久化到硬盘，用到时才加载到内存

### 3. delay queue 延时队列
实现方式有两种：死信队列  、 延时插件

### 4. dead queue 死信队列

## 二、 Exchange交换机类型