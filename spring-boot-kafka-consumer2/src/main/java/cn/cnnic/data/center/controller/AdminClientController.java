package cn.cnnic.data.center.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;


/**
 * 通过操作API来动态创建主题、分区等信息
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminClientController {

    @Autowired // adminClien需要自己生成配置bean
    private AdminClient adminClient;


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaProperties properties;

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
    public Map<TopicPartition, Long> getConsumerInfo(@PathVariable("topicName") String topicName) throws ExecutionException, InterruptedException, TimeoutException  {
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

        String groupID = "mykafka3";

        //获取每个分区的最新消费offset
        Map<TopicPartition, OffsetSpec> requestLatestOffsets = new HashMap<>();
        Map<TopicPartition, OffsetAndMetadata> offsets = adminClient.listConsumerGroupOffsets("mykafka3").partitionsToOffsetAndMetadata().get();
        ListConsumerGroupOffsetsResult result = adminClient.listConsumerGroupOffsets(groupID); // 获取给定消费者组的最新消费消息的位移
      //  for(TopicPartition tp: offsets.keySet()) {
       //     requestLatestOffsets.put(tp, OffsetSpec.latest());

       // }


        Map<TopicPartition, OffsetAndMetadata> consumedOffsets = result.partitionsToOffsetAndMetadata().get(10, TimeUnit.SECONDS);


        KafkaConsumer kc = getNewConsumer();

      //  try (final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            Map<TopicPartition, Long> endOffsets = kc.endOffsets(consumedOffsets.keySet());// 获取订阅分区的最新消息位移

        //某个分区，未消费的个数
            Map<TopicPartition, Long> mm = endOffsets.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue() - consumedOffsets.get(entry.getKey()).offset()));
            //endOffsets.entrySet().stream().coll
        System.out.println(mm);
       // }




       // Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latestOffsets = adminClient.listOffsets(requestLatestOffsets).all().get();

        //for (Map.Entry<TopicPartition, OffsetAndMetadata> e: offsets.entrySet()) {
         //   long latestOffset = latestOffsets.get(e.getKey()).offset();
        //    System.out.println(e.getKey()+"-->" + latestOffset);
      //  }
        System.out.println("========================================");

    return mm;

    }


    public  KafkaConsumer getNewConsumer(){
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.81.133:9092");
        props.put("group.id", "mykafka3");
        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "earliest");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        return consumer;
    }




    /**
     * 获取分组下的表述信息
     *
     * @param topicName
     * @return
     * @author liquan_pgz@qq.com
     * date 2021-01-22
     **/
    @GetMapping("/getCurrentOffset/{topicName}")
    private  long[] getDescribe(@PathVariable("topicName") String topicName) {
        long[] describe = new long[3];
        Consumer<Integer, String> consumer = getNewConsumer();
       //\ for (String topic : topics) {
            List<PartitionInfo> partitionInfos = kafkaTemplate.partitionsFor(topicName);
            List<TopicPartition> tp = new ArrayList<>();
            partitionInfos.forEach(str -> {

                TopicPartition topicPartition = new TopicPartition(topicName, str.partition());

                tp.add(topicPartition);



                long logEndOffset = consumer.endOffsets(tp).get(topicPartition);
                consumer.assign(tp);
                consumer.position(topicPartition);
                long currentOffset = consumer.position(topicPartition);


                log.info("---------------------------------------------------");
                log.info("当前GroupId---->{}",consumer.groupMetadata().groupId());
                log.info("当前分区------>{}",topicPartition.partition());
                log.info("currentOffset--->{}",currentOffset);
                log.info("logEndoffset---->{}",logEndOffset);
                log.info("未消费的offset---->{}",(logEndOffset-currentOffset));
                log.info("---------------------------------------------------");

               // describe[0] += currentOffset;
               // describe[1] += logEndOffset;
               // describe[2] = describe[1] - describe[0];
            });
       // }
        log.info(Arrays.toString(describe));
        return describe;
    }




    public ConsumerFactory<Integer, String> consumerFactory() {

        Map<String, Object> map = properties.buildConsumerProperties();

        DefaultKafkaConsumerFactory<Integer, String> factory = new DefaultKafkaConsumerFactory<>( map);

        return factory;

    }
}
