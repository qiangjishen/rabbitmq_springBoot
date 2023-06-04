package cn.cnnic.data.center.controller;



import kafka.common.OffsetAndMetadata;
import kafka.coordinator.group.GroupMetadataKey;
import kafka.coordinator.group.GroupMetadataManager;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;


import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Properties;

/**
 * @author: wangjian
 * @date: 2018-08-01 15:19
 */
public class KafkaCli {
    public static void main(String[] args) {
        Properties props = new Properties();

        props.put("bootstrap.servers", "192.168.81.133:9092");
        //每个消费者分配独立的组号
        props.put("group.id", "test_1");

        //如果value合法，则自动提交偏移量
        props.put("enable.auto.commit", "true");

        //设置多久一次更新被消费消息的偏移量
        props.put("auto.commit.interval.ms", "1000");

        //设置会话响应的时间，超过这个时间kafka可以选择放弃消费或者消费下一条消息
        props.put("session.timeout.ms", "30000");

        //该参数表示从头开始消费该主题
        props.put("auto.offset.reset", "earliest");

        //注意反序列化方式为ByteArrayDeserializer
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Collections.singletonList("__consumer_offsets"));

        System.out.println("Subscribed to topic " + "__consumer_offsets");

        while (true) {
            ConsumerRecords<byte[], byte[]> records = consumer.poll(100);
            for (ConsumerRecord<byte[], byte[]> record : records) {
//                这里直接将record的全部信息写到System.out打印流中
//                GroupMetadataManager.OffsetsMessageFormatter formatter = new GroupMetadataManager.OffsetsMessageFormatter();
//                formatter.writeTo(record, System.out);

                //对record的key进行解析，注意这里的key有两种OffsetKey和GroupMetaDataKey
                //GroupMetaDataKey中只有消费者组ID信息，OffsetKey中还包含了消费的topic信息

/**
                BaseKey key = GroupMetadataManager.readMessageKey(ByteBuffer.wrap(record.key()));
                if (key instanceof OffsetKey) {
                    GroupTopicPartition partition = (GroupTopicPartition) key.key();
                    String topic = partition.topicPartition().topic();
                    String group = partition.group();

                    System.out.println("group : " + group + "  topic : " + topic);
                    System.out.println(key.toString());
                } else if (key instanceof GroupMetadataKey) {
                    System.out.println("groupMetadataKey:------------ "+key.key());
                }
**/
                //对record的value进行解析
                OffsetAndMetadata om = GroupMetadataManager.readOffsetMessageValue(ByteBuffer.wrap(record.value()));
                //System.out.println(om.toString());


            }
        }
    }
}
