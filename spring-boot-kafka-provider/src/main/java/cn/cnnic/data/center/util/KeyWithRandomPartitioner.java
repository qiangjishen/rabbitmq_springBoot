package cn.cnnic.data.center.util;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;
import java.util.Random;

public class KeyWithRandomPartitioner implements Partitioner {

    private Random r;

    @Override
    public void configure(Map<String, ?> configs) {
        r = new Random();
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // cluster.partitionCountForTopic 表示获取指定topic的分区数量
        return r.nextInt(1000) % cluster.partitionCountForTopic(topic);
    }

    @Override
    public void close() {

    }
}

