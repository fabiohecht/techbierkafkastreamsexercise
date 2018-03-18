package ch.ipt.techbier.kafkastreams;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class StreamExpenses {
    static final Logger log = LoggerFactory.getLogger(StreamExpenses.class);

    private static final String INPUT_TOPIC_EXPENSE = "ipt-spesen-avro";
    private static final String OUTPUT_TOPIC_EMPLOYEE_EXPENSE = "employee-expense-avro";

    private static Properties config = new Properties();

    static public void main(String[] args) {
        initializeConfig();
        startStream();
    }

    static void initializeConfig() {

        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        config.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");

        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "StreamExpenses-v1");
        config.put(StreamsConfig.CLIENT_ID_CONFIG, "StreamExpenses-v1");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);

        // Enable record cache of size 10 MB.
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L);

        // Set commit interval to 1 second
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);

    }

    private static void startStream() {

        log.info("starting kafka streams");

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, Expense> expenseStream = builder.stream(INPUT_TOPIC_EXPENSE);

        final KTable<String, SumValue> expensePerEmployeeTable =
                expenseStream

                        // for demo/debugging purposes, output what has come in through the KStream (does not change stream)
                        .peek((k, v) -> log.debug(" \uD83D\uDCB5 \uD83D\uDCB8 new expense: {}", v.toString()))

                        // aggregations are done with groupBy then reduce or aggregate
                        .groupBy((s, expense) -> expense.getEmployeeAcronym())
                        .reduce((v1, v2) -> {
                            v1.setAmount(v1.getAmount() + v2.getAmount());
                            return v1;
                        })

                        // converts values from Expense to SumValue
                        .mapValues(expense ->
                                SumValue.newBuilder()
                                        .setEmployeeAcronym(expense.getEmployeeAcronym())
                                        .setSumAmount(expense.getAmount())
                                        .build()
                        );

        // for demo/debugging purposes, output what we are writing to the KTable
        expensePerEmployeeTable
                .toStream()
                .peek((k, v) -> log.debug(" total: {} {}", k, v.toString()));

        // output KTable to topic
        expensePerEmployeeTable.toStream().to(OUTPUT_TOPIC_EMPLOYEE_EXPENSE);

        // starts stream
        final KafkaStreams streamsContracts = new KafkaStreams(builder.build(), config);

        streamsContracts.cleanUp();
        streamsContracts.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streamsContracts::close));
    }
}