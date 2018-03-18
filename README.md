
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

**If you want to start reprocessing from scratch**

1. stop streams app + producer
2. (optional) type kafka-streams-application-reset to understand what the tool does
3. use the application reset tool
        
        kafka-streams-application-reset --bootstrap-servers localhost:9092 --application-id StreamExpenses-v1 --input-topics ipt-spesen-avro
        
4. clear local store: 

        rm -rf /tmp/kafka-streams/StreamExpenses-v1

Note: This does not clear the input topic, so the Streams app will process all messages on start.

If you want to clear input topic, you could just delete it. It will be autocreated by the producer when run again.

    kafka-topics --zookeeper localhost:2181 --delete --topic ipt-spesen-avro

# Exercise 7

Let's join a KStream with a KTable!

Use the kafka-console-producer to produce the file src/main/resources/text/employees.txt as key:values to a topic called "employees". The topic will be auto-created (cluster configuration allows it).

     cat employees.txt | kafka-console-producer --topic employees --broker-list localhost:9092 --property key.separator=: --property parse.key=true

Modify SumValue.avsc to include the full name. Note how an optional field can be added in Avro in a backwards-compatible way.

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


