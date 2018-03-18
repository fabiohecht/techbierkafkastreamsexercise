
# Exercise 6

This is a "simple" Kafka Streams application demo.

It consumes from a topic named "expense" (null keys, value Avro ch.ipt.techbier.kafkastreams.Expense)
and produces to topic "employeeexpense" (String key, value Avro ch.ipt.techbier.kafkastreams.SumValue) 
simply adding all expenses for each employee.

**Look at code**

Pretty much the whole implementation is in [ch.ipt.techbier.kafkastreams.StreamExpenses](src/main/java/ch/ipt/techbier/kafkastreams/StreamExpenses.java).

Have a look at the implementation to see how the KStream containing all expenses is aggregated to produce a KTable with sums.

**Run the Kafka Streams Application**

Kafka Streams application are normal Java applications. Just run StreamExpenses's main method.
In IntelliJ you can use Shift+F10 or click the green play icon.

**Run a console consumer**

Use two terminal windows to view the contents of the in topic.

**Run the Java Avro producer**

Run the producer built before with Avro.

# Exercise 7

In src/main/resources/avro

1. Create an "employee" topic with both key and values Strings. This will be used as input by the Kafka Streams app.
2. Use the kafka-console-producer to produce the file src/main/resources/text/employees.txt as key:values.
3. Create an "employeeexpensename" topic with both key and values Strings. This will be used as output by the Kafka Streams app.
4. Modify StreamExpenses to output the full name of the Employee instead of the acronym. Hints:
  - Use builder.table to create a KTable variable for the "employee" topic.
  - The output KTable will be <String, String>
  - Use a .join() immediately after the .reduce 


**Extra credit**

Level: Hard.

Create another output with Avro value type that outputs the average.

Hints:




to start reprocessing:

1. stop streams app + producer
2. (optional) type kafka-streams-application-reset to understand what the tool does
3. kafka-streams-application-reset --application-id StreamExpenses-v1 \
                                      --input-topics iptspesenavro6 \
                                      --bootstrap-servers localhost:9092
4. clear local store: rm -rf /tmp/kafka-streams/StreamExpenses-v1

to clear input topic, you could just delete it. It will be autocreated by the producer when run again.
    kafka-topics --zookeeper localhost:2181 --delete --topic iptspesenavro6



kafka-avro-console-consumer --bootstrap-server localhost:9092 --topic employeeexpenseavro5 --from-beginning