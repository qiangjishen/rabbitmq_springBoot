package cn.cnnic.data.center.controller;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Properties;

@RestController
@RequestMapping("/kafka")
@Slf4j
public class KafkaConsumerController {

    /**
     * @KafkaListener注解所标注的方法并不会在IOC容器中被注册为Bean，
     * 而是会被注册在KafkaListenerEndpointRegistry中，
     * 而KafkaListenerEndpointRegistry在SpringIOC中已经被注册为Bean
     **/
    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private ConsumerFactory consumerFactory;


    @GetMapping("/start")
    public String startConsume() {
        //创建kafka consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(buildKafkaConfig());
        try {

            String topic = "topic.hangge.demo";

            int partitionIndex = 0;
            //声明kafka分区数相等的消费线程数，一个分区对应一个消费线程
            int consumeThreadNum = 9;
            //特殊指定每个分区开始消费的offset
            List<Integer> partitionOffsets = Lists.newArrayList(106, 0, 0, 0, 0, 0, 0, 0, 0);

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

        return "success";
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

                //offset提交, offset保存到数据库，放到同一个事务中。
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



    /**
     * 定时启动监听器
     * @param
     * @author yh
     * @date 2022/5/11
     * @return
     */
    @GetMapping("/startListener")
    public void startListener() {

        System.out.println("-->"+registry.getListenerContainer("demoContainer3").isRunning());
        System.out.println(registry.getListenerContainer("demoContainer3").isContainerPaused());

        // "timingConsumer"是@KafkaListener注解后面设置的监听器ID,标识这个监听器
        if (!registry.getListenerContainer("demoContainer3").isRunning() ) {
            registry.getListenerContainer("demoContainer3").start();
        }
        if ( registry.getListenerContainer("demoContainer3").isContainerPaused()) {
            registry.getListenerContainer("demoContainer3").resume();
        }
        //registry.getListenerContainer("timingConsumer").resume();
    }

    /**
     * 定时停止监听器
     * @param
     * @author yh
     * @date 2022/5/11
     * @return
     */
    @GetMapping("/stop")
    public void shutDownListener() {

        registry.getListenerContainer("demoContainer3").pause();
    }


}
