package cn.datalake.KafkaProducerDemo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * describe: Kerberos环境下通过Kafka的Bootstrap.Server消费数据
 * creat_date: 2019/06/11
 */
public class ConsumerTest {

    public static String confPath = System.getProperty("user.dir")  + File.separator + "conf";

    public static void main(String[] args) {
        String krb5conf = confPath + File.separator + "krb5.conf";
        String jaasconf = confPath + File.separator + "jaas.conf";
        Properties appProperties = new Properties();
        try {
            appProperties.load(new FileInputStream(new File(confPath + File.separator + "app.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String brokerlist = String.valueOf(appProperties.get("bootstrap.servers"));
        String topic_name = String.valueOf(appProperties.get("topic.name"));

        System.setProperty("java.security.krb5.conf", krb5conf);
        System.setProperty("java.security.auth.login.config", jaasconf);
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
//        System.setProperty("sun.security.krb5.debug", "true");

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerlist);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "DemoConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.kerberos.service.name", "kafka");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

        TopicPartition partition0 = new TopicPartition(topic_name, 0);
        TopicPartition partition1 = new TopicPartition(topic_name, 1);
        TopicPartition partition2 = new TopicPartition(topic_name, 2);

        consumer.assign(Arrays.asList(partition0, partition1, partition2));

        ConsumerRecords<String, String> records = null;

        while (true) {
            try {
                Thread.sleep(10000l);
                System.out.println();
                records = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
