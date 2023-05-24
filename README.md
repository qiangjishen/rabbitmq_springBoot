# rabbitmq跟rocketmq应用实例
# MQ实际应用场景

## 如何保证Producer的可靠性投递 （如何保证 插入实际业务库的同时向MQ发送消息）

 1. 保证消息的成功发出
 2. 保证MQ节点的成功接收
 3. 发送端收到MQ节点(Broker) 确认应答
 4. 完善的消息补偿机制
  
  在实际生产中，很难保障前三点的完全可靠，比如在极端的环境中，生产者发送消息失败了，发送端在接受确认应答时突然发生网络闪断等等情况，很难保障可靠性投递，所以就需要有第四点完善的消息补偿机制。

### 解决方案：
#### 1.方案一:消息信息落库,对消息状态进行打标(常见方案)
![image](https://github.com/qiangjishen/rabbitmq_springBoot/assets/4744404/04729baa-9701-456f-8795-d7e23a1b2813)
方案实现流程
比如我下单成功
step1 - 对订单数据入BIZ DB订单库,并对因此生成的业务消息入MSG DB消息库

此处由于采用了两个数据库,需要两次持久化操作,为了保证数据的一致性,有人可能就想着采用分布式事务,但在大厂实践中,基本都是采用补偿机制!

这里一定要保证
1. step1 中消息都存储成功了，没有出现任何异常情况，然后生产端再进行消息发送。如果失败了就进行快速失败机制对业务数据和消息入库完毕就进入
2. setp2 - 发送消息到 MQ 服务上，如果一切正常无误消费者监听到该消息，进入
3. step3 - 生产端有一个Confirm Listener,异步监听Broker回送的响应,从而判断消息是否投递成功
4. step4 - 如果成功,去数据库查询该消息,并将消息状态更新为1
5. step5 - 如果出现意外情况，消费者未接收到或者 Listener 接收确认时发生网络闪断，导致生产端的Listener就永远收不到这条消息的confirm应答了，也就是说这条消息的状态就一直为0了，这时候就需要用到我们的分布式定时任务来从 MSG 数据库抓取那些超时了还未被消费的消息，重新发送一遍，此时我们需要设置一个规则，比如说消息在入库时候设置一个临界值timeout，5分钟之后如果还是0的状态那就需要把消息抽取出来。这里我们使用的是分布式定时任务，去定时抓取DB中距离消息创建时间超过5分钟的且状态为0的消息。
 6. step6 - 把抓取出来的消息进行重新投递(Retry Send)，也就是从第二步开始继续往下走

 7. step7 - 当然有些消息可能就是由于一些实际的问题无法路由到Broker，比如routingKey设置不对，对应的队列被误删除了，那么这种消息即使重试多次也仍然无法投递成功，所以需要对重试次数做限制，比如限制3次，如果投递次数大于三次，那么就将消息状态更新为2，表示这个消息最终投递失败,然后通过补偿机制，人工去处理。实际生产中，这种情况还是比较少的，但是你不能没有这个补偿机制，要不然就做不到可靠性了。

思考:该方案在高并发的场景下是否合适
对于第一种方案，我们需要做两次数据库的持久化操作，在高并发场景下显然数据库存在着性能瓶颈.

其实在我们的核心链路中只需要对业务进行入库就可以了，消息就没必要先入库了，我们可以做消息的延迟投递，做二次确认，回调检查。下面然我们看方案二
#### 2. 消息延迟投递,两次确认,回调检查(大规模海量数据方案)
大厂经典实现方案

当然这种方案不一定能保障百分百投递成功，但是基本上可以保障大概99.9%的消息是OK的，有些特别极端的情况只能是人工去做补偿了，或者使用定时任务.

主要就是为了减少DB操作
![image](https://github.com/qiangjishen/rabbitmq_springBoot/assets/4744404/e5392057-f00a-4fff-b8a4-89cf29e25d96)

### RabbitMQ work队列公平模式
这里的配置两个都不能少，否则按照平均分配：

#消息确认机制更改为手动
spring.rabbitmq.listener.simple.acknowledge-mode=manual     

#预处理模式更改为每次读取1条消息,在消费者未回执确认之前,不在进行下一条消息的投送
spring.rabbitmq.listener.simple.prefetch=1

### 小结
这两种方案都是可行的，需要根据实际业务来进行选择,方案二也是互联网大厂更为经典和主流的解决方案.但是若对性能要求不是那么高,方案一要更简单.
