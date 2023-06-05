package cn.cnnic.data.center.controller;


import cn.cnnic.data.center.dto.OffsetDto;
import cn.cnnic.data.center.dto.OrderDto;
import cn.cnnic.data.center.service.TopicOffsetService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 手动摘取  ，
 * 提交offset
 */
@RestController
@RequestMapping("/manual")
@Slf4j
public class ManualConsumController {

    private static String groupId = "group_cnnic";

    private static String clientId = "client_999";

    private String topic = "topic.ppp";

    private boolean isRunning = true;

    private KafkaConsumer<String, String> kafkaConsumer;


    protected static Properties initConfig(){
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.81.133:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG,clientId);
        //默认一次poll 500条消息
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 2);
        //因为如果两次poll的时间如果超出了30s的时间间隔，kafka会认为其消费能力过弱，将其踢出消费组。将分区分配给其他消费者。
        properties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 30 * 1000);

        return properties;
    }

    @PostConstruct
    public void init(){
        kafkaConsumer = new KafkaConsumer<>(initConfig());
        kafkaConsumer.subscribe(Arrays.asList(topic));
    }

    @GetMapping("/mmm/{num}")
    public void manuamConsum(@PathVariable Long num){


        Set<TopicPartition> assignment = new HashSet<>();
        while (assignment.size() == 0) {
            kafkaConsumer.poll(100L);
            assignment = kafkaConsumer.assignment();
            System.out.println("topics-->"+assignment);
        }

        System.out.println(kafkaConsumer.endOffsets(assignment));

        for (TopicPartition tp : assignment) {
            //Long offset = num; //getOffsetFromDB(tp);
            Long offset = offsetService.getPartitionByTopic(tp.topic(), tp.partition());

            kafkaConsumer.seek(tp,offset);

        }


      //  while (isRunning) {
            //如果每隔1s内没有poll到任何消息，则继续去poll消息，循环往复，直到poll到消息。如果超出了1s，则此次⻓轮询结束
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis( 1000 )); //
            Set<TopicPartition> partitions = consumerRecords.partitions();
            for (TopicPartition tp : partitions) {
                List<ConsumerRecord<String, String>> records = consumerRecords.records(tp);
                for (ConsumerRecord<String, String> record : records) {
                    //process record
                    System.out.println("处理数据::: 分区--->"+record.partition()+"    offset-->"+record.offset()+"  value:"+record.value());
                    kafkaConsumer.commitSync();
                }
                long lastConsumedOffset = records.get(records.size() - 1).offset();
                //保存位移
                storeOffsetToDB(tp,lastConsumedOffset+1);
            }
      //  }

    }


    private  void storeOffsetToDB(TopicPartition tp, Long offset) {
        System.out.println("--保存:-------分区 ："+tp.partition()+"-----------OFFSET：：：:"+offset);

        OffsetDto odto = new OffsetDto();
        odto.setGroupId(kafkaConsumer.groupMetadata().groupId());
        odto.setTopic(tp.topic());
        odto.setPartitions(tp.partition());
        odto.setOffset(offset);
        odto.setUpdateTime(LocalDateTime.now());

        OrderDto orderDto = new OrderDto();
        orderDto.setKeyy("kkkkkk");
        orderDto.setOrderId(Long.valueOf(22));
        orderDto.setContext("context.......");

        offsetService.update(odto,orderDto);


    }

    private  Long getOffsetFromDB(TopicPartition tp) {
        return 0L;
    }

    /**===================
     * =======当某一个分区消息堆积了，怎么办？=========
     * 这个时候也无法增加消费端，因为一个分区只能有一个消费者。
     * 解决办法：
     *    只能是起一个线程进行拉取，然后起一个线程池进行消费。
     *    注意：如果是有序的就不行了。
     */


    @Autowired
    private TopicOffsetService offsetService;

    /**
     * 同时保存offset、业务数据，保证原子性
     * @param num
     */
    @GetMapping("/save/{num}")
    public void saveOffset(@PathVariable Long num){

        OffsetDto setDto = new OffsetDto();
        setDto.setGroupId(String.valueOf(1));
        setDto.setTopic("topic1");
        setDto.setPartitions(10);
        setDto.setOffset(Long.valueOf(111));
        setDto.setUpdateTime(LocalDateTime.now());

        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(Long.valueOf("22222"));
        orderDto.setKeyy("order_key_1111");
        orderDto.setContext("apple iphone");
        orderDto.setUpdateTime(LocalDateTime.now());


        offsetService.save(setDto, orderDto);


    }


    @GetMapping("/list/{topic}/{num}")
    public List<OffsetDto> listFutureGoods(@PathVariable String topic, @PathVariable Integer num) {
        //PageHelper.startPage(page.getPageNum(), page.getPageSize());

        List<OffsetDto> list = offsetService.list(topic, num);
        PageInfo<OffsetDto> pageInfo = new PageInfo<>(list);

        return list;
    }


}
