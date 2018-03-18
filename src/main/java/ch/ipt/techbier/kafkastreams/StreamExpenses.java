package ch.ipt.techbier.kafkastreams;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class StreamExpenses {
    static final Logger log = LoggerFactory.getLogger(StreamExpenses.class);

    private static final String INPUT_TOPIC_EXPENSE = "iptspesenavro5";
    private static final String OUTPUT_TOPIC_EMPLOYEE_EXPENSE = "employeeexpenseavro5";

    private static final String INPUT_TOPIC_EMPLOYEE = "employee";
    private static final String OUTPUT_TOPIC_EMPLOYEE_EXPENSE_NAME = "employeeexpensename";

    final static Serde<String> stringSerde = Serdes.String();
    final static Serde<Integer> integerSerde = Serdes.Integer();
    final static Serde<Expense> expenseSpecificAvroSerde = new SpecificAvroSerde<>();

    private static Properties config = new Properties();

    static public void main(String[] args) {
        initializeConfig();
        startStream();
    }

    static void initializeConfig() {

        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://127.0.0.1:8081");
        config.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");

        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "StreamExpenses-v2");
        config.put(StreamsConfig.CLIENT_ID_CONFIG, "StreamExpenses-v2");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
//        config.put("key.serializer", StringSerializer.class.getName());
//        config.put("value.serializer", KafkaAvroSerializer.class.getName());

        // Enable record cache of size 10 MB.
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L);

        // Set commit interval to 1 second.
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);

    }

    private static void startStream() {

        log.info("starting kafka streams");

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, Expense> expenseStream = builder.stream(INPUT_TOPIC_EXPENSE/*, Consumed.with(stringSerde, expenseSpecificAvroSerde)*/);

        final KTable<String, SumValue> expensePerEmployeeTable =
                expenseStream

                        // for demo purposes
                        .peek((k, v) -> log.debug(" \uD83D\uDCB5 \uD83D\uDCB8 new expense: {}", v.toString()))

//                        //try with groupBy
                .groupBy((s, expense) -> expense.getEmployeeAcronym())
                        //.aggregate(() -> 0, (s, expense, integer) -> 1, Materialized.with(stringSerde, integerSerde));
                        // set our wanted keys and values
//                .mapValues((v) -> v.getAmount())

//                .reduce(
//                        (v1, v2) -> v1.getAmount() + v2.getAmount(),
//                )
//                .mapValues(Expense::getAmount);
//
//
//                        .map((k, v) -> new KeyValue<>(v.getEmployeeAcronym(), v.getAmount()))
//
//                        //try with groupByKey and reduce

//                        .groupByKey()
                        .reduce((v1, v2) -> {
                            v1.setAmount(v1.getAmount() + v2.getAmount());
                            return v1;
                        })
                        .mapValues(expense -> {
                            return SumValue.newBuilder()
                                    .setEmployeeAcronym(expense.getEmployeeAcronym())
                                    .setSumAmount(expense.getAmount())
                                    .build();
                        });



        expensePerEmployeeTable
                .toStream()
                .peek((k, v) -> log.debug(" ended: {}", v.toString()));


        //output KTable to topic
        expensePerEmployeeTable.toStream().to(OUTPUT_TOPIC_EMPLOYEE_EXPENSE);

        final KafkaStreams streamsContracts = new KafkaStreams(builder.build(), config);

        streamsContracts.cleanUp();
        streamsContracts.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streamsContracts::close));
    }

    private static void startStream2() {

        log.info("starting kafka streams 2");

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, Expense> expenseStream = builder.stream(INPUT_TOPIC_EXPENSE);
        final KTable<String, String> employeeTable = builder.table(INPUT_TOPIC_EMPLOYEE, Materialized.with(stringSerde, stringSerde));

        final KTable<String, String> expensePerEmployeeTable = expenseStream

                // for demo purposes
                .peek((k, v) -> log.debug(" \uD83D\uDCB5 \uD83D\uDCB8 new expense: %s\n", v.toString()))

                // set our wanted keys and values
                .map((k, v) -> new KeyValue<>(v.getEmployeeAcronym(), v.getAmount()))

                //KStream aggregations such as `reduce` operate on a per-key basis
                .groupByKey()
                .reduce((v1, v2) -> v1 + v2)

                .join(
                        employeeTable,
                        //for simplicity, we use a String, a more elegant solution would use another Object instead
                        (amount, employeeName) -> employeeName + " -> " + amount
                );

        //output KTable to topic
        expensePerEmployeeTable.toStream().to(OUTPUT_TOPIC_EMPLOYEE_EXPENSE_NAME);

        final KafkaStreams streamsContracts = new KafkaStreams(builder.build(), config);

        streamsContracts.cleanUp();
        streamsContracts.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streamsContracts::close));
    }
}