
# Exercise 6

This is a "simple" Kafka Streams application demo.

It consumes from a topic named "ipt-spesen-avro" (null keys, value Avro ch.ipt.techbier.kafkastreams.Expense)
and produces to topic "employee-expense-avro" (String key, value Avro ch.ipt.techbier.kafkastreams.SumValue) 
simply adding all expenses for each employee.

1. Look at the code

   - Pretty much the whole implementation is in [ch.ipt.techbier.kafkastreams.StreamExpenses](src/main/java/ch/ipt/techbier/kafkastreams/StreamExpenses.java).

   - Have a look at the implementation to see how the KStream containing all expenses is aggregated to produce a KTable with sums per employee.

2. Run the Kafka Streams Application

   - Kafka Streams application are normal Java applications. Just run StreamExpenses's main method.
In IntelliJ you can use Shift+F10 or click the green play icon.

3. Run the producer and check out the action

   - Run the producer built before with Avro. It doesn't matter who starts first (the streams app or the producer).

   - Use two terminal windows to view the contents of both inbound and outbound topics (kafka-avro-console-consumer).

   - Example:

         kafka-avro-console-consumer --bootstrap-server localhost:9092 --topic employee-expense-avro --from-beginning

If you stop the streams app and re-run, it will pick up where it left off. 

**If you want to start reprocessing from scratch**

1. Stop streams app + producer
2. (optional) Type kafka-streams-application-reset to understand what the tool does
3. Use the application reset tool
        
        kafka-streams-application-reset --bootstrap-servers localhost:9092 --application-id StreamExpenses-v1 --input-topics ipt-spesen-avro
        
4. Clear local store: 

        rm -rf /tmp/kafka-streams/StreamExpenses-v1

Note: This does not clear the input topic, so the Streams app will process all messages on start.

If you want to clear input topic, you could just delete it. It will be autocreated by the producer when run again.

    kafka-topics --zookeeper localhost:2181 --delete --topic ipt-spesen-avro

Note 2: This does not clear schemas registered for the topic in the schema registry. If you need to delete them, you can use the Landoop UI or the Schema Registry API.

# Exercise 7

Let's join a KStream with a KTable!

Use the kafka-console-producer to produce the file src/main/resources/text/employees.txt as key:values to a topic called "employees". The topic will be auto-created (cluster configuration allows it).

     cat employees.txt | kafka-console-producer --topic employees --broker-list localhost:9092 --property key.separator=: --property parse.key=true

Modify SumValue.avsc to include the full name. Note how an optional field can be added in Avro in a backwards-compatible way. This means both the old and the new version of the streams app may run.

    {
      "name": "employeeFullName",
      "doc": "Full name of the employee who spent the money.",
      "type": ["null", "boolean"],
      "default":null
    }

Do a maven compile to regenerate Java classes.

Modify StreamExpenses to output the full name of the Employee along with the acronym.

1. Add the constant:

       private static final String INPUT_TOPIC_EMPLOYEE = "employees";

2. Create a KTable for the employees topic. Here we need to specify a Serde (serializer/deserializer) because it differs from the defaults (String/Avro).

       Serde<String> stringSerde = Serdes.String();
       final KTable<String, String> employeeTable = builder.table(INPUT_TOPIC_EMPLOYEE, Materialized.with(stringSerde, stringSerde));

3. Add the following after the mapValues:

        .join(
                employeeTable,
                (expense, name) -> {
                    expense.setEmployeeFullName(name);
                    return expense;
                });

4. Run StreamExpenses and observe results with kafka-avro-console-consumer

The above solution is implemented in the "join" branch.

# Exercise 8

We will create a Kafka Connector using the Landoop UI and create a file sink connector, because it's easy.

1. Open up Landoop [Kafka Connect UI](http://localhost:3030/kafka-connect-ui/#/)
2. Click NEW
3. Under Sinks, choose File
4. Use the following configuration

        name=FileStreamSinkConnector
        connector.class=org.apache.kafka.connect.file.FileStreamSinkConnector
        topics=employee-expense-avro
        tasks.max=1
        file=/tmp/employee-expense-avro
        key.converter=org.apache.kafka.connect.storage.StringConverter
        value.converter=io.confluent.connect.avro.AvroConverter
        value.converter.schema.registry.url=http://localhost:8081

5. Click CREATE
6. Make sure the streams app and the producer are running, so events are produced.
7. On a terminal window, watch what is added to the file:

        tail -f /tmp/employee-expense-avro

The same configuration properties could be used with Kafka Connect's API or command line interface.

If you have time, you can play around with other connectors. 
