package cn.cnnic.data.center.service;


import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.CommandLineRunner;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * 手动消费，保存offset
 */
@Service
@Slf4j
public class TspLogbookAnalysisService implements CommandLineRunner {
    //声明kafka分区数相等的消费线程数，一个分区对应一个消费线程
    private static final int consumeThreadNum = 9;
    //特殊指定每个分区开始消费的offset
    private List<Integer> partitionOffsets = Lists.newArrayList(186, 0, 0, 0, 0, 0, 0, 0, 0);

    private ExecutorService executorService = Executors.newFixedThreadPool(consumeThreadNum);

    @Override
    public void run(String... args) {
        System.out.println("自定义函数run...................................");
        //循环遍历创建消费线程
        IntStream.range(0, consumeThreadNum)
                .forEach(partitionIndex -> executorService.submit(() -> startConsume(partitionIndex)));

    }



    private void startConsume(int partitionIndex) {
        //创建kafka consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(buildKafkaConfig());

        try {

            String topic = "topic.hangge.demo";

            //指定该consumer对应的消费分区
            TopicPartition partition = new TopicPartition(topic, partitionIndex);
            consumer.assign(Lists.newArrayList(partition));

            //consumer的offset处理
            if (CollectionUtils.isNotEmpty(partitionOffsets)  &&  partitionOffsets.size() == consumeThreadNum) {
                Integer seekOffset = partitionOffsets.get(partitionIndex);
                log.info("partition:{} , offset seek from {}", partition, seekOffset);
                consumer.seek(partition, seekOffset);

            }

            //开始消费数据任务
            kafkaRecordConsume(consumer, partition);
        } catch (Exception e) {
            log.error("kafka consume error:{}", e.getStackTrace());
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }


    private void kafkaRecordConsume(KafkaConsumer<String, String> consumer, TopicPartition partition) {
        while (true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(3000);
                //具体的处理流程
                records.forEach((k) -> handleKafkaInput(k.key(), k.value()));

                //🌿很重要：日志记录当前consumer的offset，partition相关信息(之后如需重新指定offset消费就从这里的日志中获取offset，partition信息)
                if (records.count() > 0) {
                    String currentOffset = String.valueOf(consumer.position(partition));
                    log.info("current records size is：{}, partition is: {}, offset is:{}", records.count(), consumer.assignment(), currentOffset);
                }

                //offset提交
                consumer.commitAsync();
            } catch (Exception e) {
                log.error("handlerKafkaInput error{}", e.getStackTrace());
            }
        }
    }


    public  void handleKafkaInput(String key, String value ) {
        log.info("key--->{}", key);
        log.info("value--->{}", value);
    }

    /**
     * 声明kafka consumer的配置类
     * @return
     */
    private Properties buildKafkaConfig() {
        Properties kafkaConfiguration = new Properties();
        kafkaConfiguration.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.81.133:9092");
        kafkaConfiguration.put(ConsumerConfig.GROUP_ID_CONFIG, "");
        kafkaConfiguration.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5");
        //  kafkaConfiguration.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "");
        kafkaConfiguration.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaConfiguration.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        kafkaConfiguration.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");


        return kafkaConfiguration;
    }
}
