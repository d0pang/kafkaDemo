#!/usr/bin/env bash
$ java -Djava.security.auth.login.config=/usr/hdp/current/kafka-broker/config/kafka_client_jaas.conf -cp KafkaProducerDemo-1.0-SNAPSHOT.jar cn.datalake.KafkaProducerDemo.ProducerTest
$ java -Djava.security.auth.login.config=/usr/hdp/current/kafka-broker/config/kafka_client_jaas.conf -cp KafkaProducerDemo-1.0-SNAPSHOT.jar cn.datalake.KafkaProducerDemo.ConsumerTest
$ java -Djava.security.auth.login.config=/usr/hdp/current/kafka-broker/config/kafka_client_jaas.conf -cp KafkaProducerDemo-1.0-SNAPSHOT.jar cn.datalake.KafkaProducerDemo.ReadUserInfoFileToKafka

--kafka cli

-- create topic, by default, The only user with access to ZooKeeper is the service account running Kafka
export KAFKA_OPTS="-Djava.security.auth.login.config=/usr/hdp/current/kafka-broker/config/kafka_jaas.conf"
[root@hadoop3 bin]# kinit -kt /etc/security/keytabs/kafka.service.keytab kafka/hadoop3.lts.local@LTS.LOCAL
bin/kafka-topics.sh --create --zookeeper hadoop1.lts.local:2181,hadoop2.lts.local:2181,hadoop3.lts.local:2181 --partitions 3 --replication-factor 3 --topic test-topic
bin/kafka-topics.sh --list --zookeeper hadoop1.lts.local:2181,hadoop2.lts.local:2181,hadoop3.lts.local:2181
bin/kafka-topics.sh --describe --zookeeper hadoop1.lts.local:2181,hadoop2.lts.local:2181,hadoop3.lts.local:2181 --topic test-topic
bin/kafka-topics.sh --delete --zookeeper hadoop1.lts.local:2181,hadoop2.lts.local:2181,hadoop3.lts.local:2181 --topic test4

--Kafka Acls , 如果是启用Ranger来管理权限，则在Ranger进行权限配置，不需要再设置acl
Principal P is [Allowed/Denied] Operation O From Host H On Resource R
A principal is any entity that can be authenticated by the system, such as a user account, a thread or process running in the security context of a user account,
or security groups of such accounts.
Principal is specified in the PrincipalType:PrincipalName (user:dev@EXAMPLE.COM) format. Specify user:* to indicate all principals.
Principal is a comma-separated list of principals. Specify * to indicate all principals. (A principal is any entity that can be authenticated by the system,
such as a user account, a thread or process running in the security context of a user account, or security groups of such accounts.)

Operation can be one of: READ, WRITE, CREATE, DESCRIBE, or ALL.

Resource is a topic name, a consumer group name, or the string “kafka-cluster” to indicate a cluster-level resource (only used with a CREATE operation).

Host is the client host IP address. Specify * to indicate all hosts.

bin/kafka-acls.sh --authorizer-properties zookeeper.connect=hadoop1.lts.local:2181,hadoop2.lts.local:2181,hadoop3.lts.local:2181 --list
bin/kafka-acls.sh --authorizer-properties zookeeper.connect=hadoop1.lts.local:2181,hadoop2.lts.local:2181,hadoop3.lts.local:2181 --add --allow-principal User:svchadoop@LTS.LOCAL --operation All --topic test2 --group=*
bin/kafka-acls.sh --authorizer-properties zookeeper.connect=hadoop1.lts.local:2181,hadoop2.lts.local:2181,hadoop3.lts.local:2181 --add --allow-principal User:svcdata@LTS.LOCAL --operation All --cluster="kafka-cluster" --group=*
bin/kafka-acls.sh --authorizer-properties zookeeper.connect=hadoop1.lts.local:2181,hadoop2.lts.local:2181,hadoop3.lts.local:2181 --remove --allow-principal User:svchadoop@LTS.LOCAL --operation All --topic test2 --group=*

--delete topic
[root@hadoop3 bin]# kinit -kt /etc/security/keytabs/kafka.service.keytab kafka/hadoop3.lts.local@LTS.LOCAL
cd /usr/hdp/current/zookeeper-client/bin/
[root@hadoop3 bin]# ./zkCli.sh -server hadoop3:2181
[zk: hadoop3:2181(CONNECTED) 1] ls /brokers/topics
[zk: hadoop3:2181(CONNECTED) 2] rmr  /brokers/topics/test
--提示：Error while executing topic command : KeeperErrorCode = NoAuth for /config/topics/test3 先删除rmr /config/topics/test3
[zk: hadoop3:2181(CONNECTED) 5] rmr /config/topics/test

--create SASL_PLAINTEXT config file
cat security.conf
security.protocol=SASL_PLAINTEXT

--kafka producer
cd /usr/hdp/current/kafka-broker
export KAFKA_OPTS="-Djava.security.auth.login.config=/usr/hdp/current/kafka-broker/config/kafka_client_jaas.conf"
bin/kafka-console-producer.sh --broker-list hadoop3.lts.local:6667,hadoop4.lts.local:6667,hadoop5.lts.local:6667 --producer.config /home/svchadoop/kafka/security.conf --topic test1
bin/kafka-console-producer.sh --broker-list hadoop3.lts.local:6667,hadoop4.lts.local:6667,hadoop5.lts.local:6667 --producer-property security.protocol=SASL_PLAINTEXT --topic test-topic

--kafka consumer
cd /usr/hdp/current/kafka-broker
export KAFKA_OPTS="-Djava.security.auth.login.config=/usr/hdp/current/kafka-broker/config/kafka_client_jaas.conf"
bin/kafka-console-consumer.sh --bootstrap-server hadoop3.lts.local:6667,hadoop4.lts.local:6667,hadoop5.lts.local:6667 --from-beginning --consumer.config /home/svchadoop/kafka/security.conf --topic test1
bin/kafka-console-consumer.sh --bootstrap-server hadoop3.lts.local:6667,hadoop4.lts.local:6667,hadoop5.lts.local:6667 --from-beginning --consumer-property security.protocol=SASL_PLAINTEXT --topic test-topic