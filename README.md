
# Exercise n

This is a simple Kafka Streams application demo.

It consumes from a topic named "expense" (null keys, value Avro ch.ipt.techbier.kafkastreams.Expense)
and produces to topic "employeeexpense" (String key, Integer value) simply adding all expenses for each employee.

**Look at code**

The whole implementation is in ch.ipt.techbier.kafkastreams.StreamExpenses.

Have a look at the implementation to see how the KStream containing all expenses is aggregated to produce a KTable with sums.

**Run the Kafka Streams Application**

It has a simple main method that must be called.
In IntelliJ you can use Shift+F10 or click the green play icon.

**Run a console consumer**

On a terminal window, use the console consumer to view the contents of the employeeexpense topic.

**Run the Java Avro producer**

Run the producer built before with Avro.

**What's happening?**




**What's happening?**


