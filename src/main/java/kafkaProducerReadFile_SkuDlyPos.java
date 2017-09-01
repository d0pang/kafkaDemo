
/**
 * Created by Hu Feng on 2017/7/24.
 */

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringEncoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


public class kafkaProducerReadFile_SkuDlyPos extends Thread{

    private String topic;
    private String fileName;

    public kafkaProducerReadFile_SkuDlyPos(String topic, String fileName){
        super();
        this.topic = topic;
        this.fileName = fileName;
    }


    @Override
    public void run() {
        SimpleDateFormat predf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(predf.format(new Date()));// new Date()为获取当前系统时间

        Producer producer = createProducer();
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
//            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file),10*1024*1024);
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                producer.send(new KeyedMessage<String, String>(topic,String.valueOf(line), tempString+","+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                // 显示行号
//                System.out.println("line " + line + ": " + tempString);
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        SimpleDateFormat afterdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(afterdf.format(new Date()));// new Date()为获取当前系统时间
//        producer.send(new KeyedMessage<Integer, String>(topic, "message: "));

    }

    private Producer createProducer() {
        Properties properties = new Properties();
        properties.put("serializer.class", StringEncoder.class.getName());
        properties.put("partitioner.class", "kafka.producer.DefaultPartitioner");
//        本地环境
//      properties.put("zookeeper.connect", "192.168.0.160:2181,192.168.0.161:2181,192.168.0.162:2181");//声明zk
//      properties.put("metadata.broker.list", "192.168.0.160:9092,192.168.0.161:9092,192.168.0.162:9092");// 声明kafka broker
//        19环境
        properties.put("zookeeper.connect", "DOSERCN60001:2181,DOSERCN60002:2181,DOSERCN60007:2181");//声明zk
        properties.put("metadata.broker.list", "DOSERCN60003:9092,DOSERCN60004:9092,DOSERCN60005:9092,DOSERCN60006:9092");// 声明kafka broker
//        21环境
//        properties.put("zookeeper.connect", "hadoop3:2181,hadoop4:2181,hadoop5:2181");//声明zk
//        properties.put("metadata.broker.list", "hadoop3:9092,hadoop4:9092,hadoop5:9092");// 声明kafka broker
        return new Producer<Integer, String>(new ProducerConfig(properties));
    }


    public static void main(String[] args) {

        //String fileName1 = "D:/hadoop/data/kafka-data.txt";
        String fileName1 = "D:/hadoop/data/sales.txt";

        String testTopic = "dylan.sales";

        new kafkaProducerReadFile_SkuDlyPos(testTopic,fileName1).start();// 使用kafka集群中创建好的主题 test

    }

}