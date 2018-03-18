
# Exercise 6

This is a "simple" Kafka Streams application demo.

It consumes from a topic named "ipt-spesen-avro" (null keys, value Avro ch.ipt.techbier.kafkastreams.Expense)
and produces to topic "employee-expense-avro" (String key, value Avro ch.ipt.techbier.kafkastreams.SumValue) 
simply adding all expenses for each employee.

**Step 1.** Look at the code

Pretty much the whole implementation is in [ch.ipt.techbier.kafkastreams.StreamExpenses](src/main/java/ch/ipt/techbier/kafkastreams/StreamExpenses.java).

Have a look at the implementation to see how the KStream containing all expenses is aggregated to produce a KTable with sums per employee.

**Step 2.** Run the Kafka Streams Application

Kafka Streams application are normal Java applications. Just run StreamExpenses's main method.
In IntelliJ you can use Shift+F10 or click the green play icon.

**Step 3.** Run the producer and check out the action

Use two terminal windows to view the contents of both inbound and outbound topics (kafka-avro-console-consumer).

Example:

     kafka-avro-console-consumer --bootstrap-server localhost:9092 --topic employee-expense-avro --from-beginning

Run the producer built before with Avro. It doesn't matter who starts first (the streams app or the producer).
If you stop the streams app and re-run, it will pick up where it left off. 

** If you want to start reprocessing from scratch **

1. stop streams app + producer
2. (optional) type kafka-streams-application-reset to understand what the tool does
3. kafka-streams-application-reset --bootstrap-servers localhost:9092 --application-id StreamExpenses-v1 --input-topics ipt-spesen-avro
4. clear local store: rm -rf /tmp/kafka-streams/StreamExpenses-v1

Note: This does not clear the input topic, so the Streams app will process all messages on start.

If you want to clear input topic, you could just delete it. It will be autocreated by the producer when run again.

    kafka-topics --zookeeper localhost:2181 --delete --topic ipt-spesen-avro

# Exercise 7

Let's make a join in Kafka Streams!

1. Use the kafka-console-producer to produce the file src/main/resources/text/employees.txt as key:values. The topic will be auto-created (cluster configuration allows it).

     cat employees.txt | kafka-console-producer --topic employees --broker-list localhost:9092 --property key.separator=: --property parse.key=true

2. Modify StreamExpenses to output the full name of the Employee along with the acronym. Hints:
  - Use builder.table to create a KTable variable for the "employee" topic.
  - The output KTable will be <String, String>
  - Use a .join() immediately after the .reduce 


**Extra credit**

Level: Hard.

Create another output with Avro value type that outputs the average.

Hints:






