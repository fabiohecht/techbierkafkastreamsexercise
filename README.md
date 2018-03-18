
# Exercise n

This is a simple Kafka Streams application demo.

It consumes from a topic named "expense" (null keys, value Avro ch.ipt.techbier.kafkastreams.Expense)
and produces to topic "employeeexpense" (String key, Integer value) simply adding all expenses for each employee.

**Look at code**

Pretty much the whole implementation is in [ch.ipt.techbier.kafkastreams.StreamExpenses](src/main/java/ch/ipt/techbier/kafkastreams/StreamExpenses.java).

Have a look at the implementation to see how the KStream containing all expenses is aggregated to produce a KTable with sums.

**Run the Kafka Streams Application**

It has a simple main method that must be called.
In IntelliJ you can use Shift+F10 or click the green play icon.

**Run a console consumer**

On a terminal window, use the console consumer to view the contents of the employeeexpense topic.

**Run the Java Avro producer**

Run the producer built before with Avro.

**Exercise**

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

Create another output with Avro value type.

Hint: