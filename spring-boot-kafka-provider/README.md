# 一、 kafka集群不再依赖Zookeeper， 采用kraft



# 二、消费offset自己保存，手动拉取消费（我觉得实际应用中，手动消费更灵活、方便、可控）
可以指定到消费具体某一主题topic的某个分区，也可以指定具体的offset消费，最大好处就是可以溯源。可以重复消费。

    @KafkaListener(id = "consumer1",groupId = "my-group1",topicPartitions = {
    @TopicPartition(topic = "topic1", partitions = { "0" }),
    @TopicPartition(topic = "topic2", partitions = "2", partitionOffsets = @PartitionOffset(partition = "1", initialOffset = "100"))
    })
    public void listen1(String data) {
       System.out.println(data);
    }

可以具体指定消费的发发数，是否手动提交等等

     @KafkaListener(id = "demoContainer3", topics = "topic.cnnic.sdnsd", groupId = "mykafka3", idIsGroup = false, clientIdPrefix = "myClient1", concurrency = "${listen.concurrency:3}", containerFactory = "kafkaManualAckListenerContainerFactory", autoStartup = "false")
    public void listen3(ConsumerRecord<String, String> record, Acknowledgment ack) {
        System.out.println(record);
        System.out.println(record.value());

        log.info("【接受到消息][线程ID:{} 消息内容：{}]", Thread.currentThread().getId(), record.value());
        // 消息处理下游绑定事务，成功消费后提交ack
        // 手动提交offset
        ack.acknowledge();
    }

手动保存offset具体见代码：ManualConsumController


# 三、发送的时候事务，保证发送MQ跟保存MySql的原子性
采用事务的形式，可以保证操作的原子性

        // 声明事务：后面报错消息不会发出去
        kafkaTemplate.executeInTransaction(operations -> {
            //支持多个操作，同时成功，同时失败。
            kafkaTemplate.send("topic.hangge.demo_11", jsonObject.toJSONString());
            operations.send("topic1","test executeInTransaction");
            //throw new RuntimeException("fail");
           //入库
            orderService.save(order);
            return true;
        });


# 四、 kafka不支持重试及死信队列，但是spring-kafka支持

    /**
     * attempts：重试次数，默认为3。
     *
     * @Backoff delay：消费延迟时间，单位为毫秒。
     *
     * @Backoff multiplier：延迟时间系数，此例中 attempts = 4， delay = 5000， multiplier = 2 ，则间隔时间依次为5s、10s、20s、40s，最大延迟时间受 maxDelay 限制。
     *
     * fixedDelayTopicStrategy：可选策略包括：SINGLE_TOPIC 、MULTIPLE_TOPICS
     */
    @KafkaListener(id = "consumer88", topics = "topic.hangge.demo_11", groupId = "top_group9")
    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 5000, multiplier = 2),
            fixedDelayTopicStrategy = FixedDelayStrategy.SINGLE_TOPIC
    )
    public void listen88(ConsumerRecord<String, String> record) {
        // 获取消息体
        String message = record.value();
        log.info("收到测试消息，message = {}, record={}", message, JSONObject.toJSONString(record));
        // todo  写重试业务消息
        throw new RuntimeException("test kafka exception");

    }

    @DltHandler
    public void dltHandler(ConsumerRecord<String, String> record) {
        log.info("进入死信队列 record={}", record.value());
        // todo  写重试多次失败后的业务，例如短信，报警，存数据库等。
    }


# 五、 分区可以动态增加，但不能减少。