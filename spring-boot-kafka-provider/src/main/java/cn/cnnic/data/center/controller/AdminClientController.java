package cn.cnnic.data.center.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ExecutionException;


/**
 * 通过操作API来动态创建主题、分区等信息
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminClientController {

    @Autowired // adminClien需要自己生成配置bean
    private AdminClient adminClient;


    /**
     * 自定义手动创建topic和分区
     */
    @GetMapping("/add/topic/{partitionNum}")
    public void testCreateTopic(@PathVariable("partitionNum") Integer partitionNum) throws InterruptedException {
        // 这种是手动创建 //10个分区，一个副本
        // 分区多的好处是能快速的处理并发量，但是也要根据机器的配置
        NewTopic topic = new NewTopic("topic.manual.create", partitionNum, (short) 1);
        adminClient.createTopics(Arrays.asList(topic));
        Thread.sleep(1000);
    }

    /**
     * 动态增加主题分区
     */
    @GetMapping("/add/patition/{partitionNum}")
    public  void  addPatitions(@PathVariable("partitionNum") Integer partitionNum) throws ExecutionException, InterruptedException {
        String topicName = "topic.manual.create";
        Map<String, NewPartitions> partitionsMap = new HashMap<>();
        //把Partition增加到2个
        NewPartitions newPartitions = NewPartitions.increaseTo(partitionNum);
        partitionsMap.put(topicName, newPartitions);
        CreatePartitionsResult createPartitionsResult = adminClient.createPartitions(partitionsMap);
        createPartitionsResult.all().get();

    }

    /**
     * 返回主题的信息
     * @param topicName 主题名称
     * @return
     */
    @GetMapping("/getTopic/{topicName}")
    public KafkaFuture<Map<String, TopicDescription>> SelectTopicInfo(@PathVariable("topicName") String topicName) {
        DescribeTopicsResult result = adminClient.describeTopics(Arrays.asList(topicName));
        KafkaFuture<Map<String, TopicDescription>> all = result.all();
        return all;
    }

    @GetMapping("/getTopic2/{topicName}")
    public void describeTopicInfo(@PathVariable("topicName") String topicName) throws ExecutionException, InterruptedException {
        List<String> list = new ArrayList<String>();
        list.add(topicName);
        //调用方法拿到信息
        DescribeTopicsResult topic = adminClient.describeTopics(list);
        Map<String, TopicDescription> map = topic.all().get();
        for (Map.Entry<String, TopicDescription> entry : map.entrySet()) {
            System.out.println("topicName:" + entry.getValue().name()); //当前topic的名字
            System.out.println("partition num:" + entry.getValue().partitions().size()); //当前topic的partition数量
            System.out.println("listp:");
            List<TopicPartitionInfo> listp = entry.getValue().partitions(); //拿到topic的partitions相关信息
            for (TopicPartitionInfo info : listp) {
                System.out.println("----------------------------------");
                System.out.println("info.partition():" + info.partition());
                System.out.println("info.leader().id():" + info.leader().id()); //领导者所在机器id，也就是机器编号配置文件中的service id
                System.out.println("info.leader().host():" + info.leader().host()); //领导者所在机器host ip
                System.out.println("info.leader().port():" + info.leader().port()); //领导者所在机器port
                System.out.println("info.replicas():" + info.replicas());  //副本的信息，有多少会拿到多少
                //List<Node> listInfo = info.replicas();
                //输出node信息
                //for (Node n : listInfo) {
                //   System.out.println("info.id():" + n.id()); //副本所在的node id
                //    System.out.println("info.host():" + n.host()); //副本所在node的host ip
                //    System.out.println("info.port():" + n.port()); //副本所在node的port
                // }
            }
        }

    }

    @GetMapping("/getOffset1/{topicName}")
    public  long getLogEndOffset(TopicPartition topicPartition){
        KafkaConsumer<String, String> consumer= getNewConsumer();
        consumer.assign(Arrays.asList(topicPartition));
        consumer.seekToEnd(Arrays.asList(topicPartition));
        long endOffset = consumer.position(topicPartition);
        System.out.println("消费到的offset--->"+ endOffset);

        return endOffset;
    }

    /**
     * 获取某主题最新消费offset
     * @param topicName
     */
    @GetMapping("/getOffset/{topicName}")
    public void getConsumerInfo(@PathVariable("topicName") String topicName) throws ExecutionException, InterruptedException {
        List<String> list = new ArrayList<String>();
        list.add(topicName);
        //调用方法拿到信息
        DescribeTopicsResult topic = adminClient.describeTopics(list);
        Map<String, TopicDescription> map = topic.all().get();
        for (Map.Entry<String, TopicDescription> entry : map.entrySet()) {
            System.out.println("topicName:" + entry.getValue().name()); //当前topic的名字
            System.out.println("partition num:" + entry.getValue().partitions().size()); //当前topic的partition数量
            System.out.println("listp:");
            List<TopicPartitionInfo> listp = entry.getValue().partitions(); //拿到topic的partitions相关信息
            for (TopicPartitionInfo info : listp) {
                System.out.println("----------------------------------");
                System.out.println("info.partition():" + info.partition());
                System.out.println("info.leader().id():" + info.leader().id()); //领导者所在机器id，也就是机器编号配置文件中的service id
                System.out.println("info.leader().host():" + info.leader().host()); //领导者所在机器host ip
                System.out.println("info.leader().port():" + info.leader().port()); //领导者所在机器port
                System.out.println("info.replicas():" + info.replicas());  //副本的信息，有多少会拿到多少

            }
        }

        System.out.println("================offset======================");



        //获取每个分区的最新消费offset
        Map<TopicPartition, OffsetSpec> requestLatestOffsets = new HashMap<>();
        Map<TopicPartition, OffsetAndMetadata> offsets = adminClient.listConsumerGroupOffsets("mykafka2").partitionsToOffsetAndMetadata().get();

        for(TopicPartition tp: offsets.keySet()) {
            requestLatestOffsets.put(tp, OffsetSpec.latest());
        }

        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latestOffsets =
                adminClient.listOffsets(requestLatestOffsets).all().get();

        for (Map.Entry<TopicPartition, OffsetAndMetadata> e: offsets.entrySet()) {
            long latestOffset = latestOffsets.get(e.getKey()).offset();
            System.out.println(e.getKey()+"-->" + latestOffset);
        }
        System.out.println("========================================");



    }


    public static KafkaConsumer getNewConsumer(){
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.81.133:9092");
        props.put("group.id", "kafkatest");
        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "earliest");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        return consumer;
    }
}
