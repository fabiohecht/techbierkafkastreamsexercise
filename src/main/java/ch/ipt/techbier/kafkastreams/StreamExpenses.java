package ch.ipt.techbier.kafkastreams;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Reducer;
import org.apache.kafka.streams.processor.internals.DefaultStreamPartitioner;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class StreamExpenses {
    static final Logger log = LoggerFactory.getLogger(StreamExpenses.class);

    private static final String INPUT_TOPIC_EXPENSE = "expense";
    private static final String OUTPUT_TOPIC_EMPLOYEE_EXPENSE = "employeeexpense";

    private static Properties config = new Properties();

    static public void main(String[] args) {
        initializeConfig();
        startStream();
    }

    static void initializeConfig() {

        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put("schema.registry.url", "http://localhost:8081");
        config.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");

        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "StreamExpenses-v1");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        config.put("key.deserializer", StringDeserializer.class);
        config.put("value.deserializer", KafkaAvroDeserializer.class);

        // Enable record cache of size 10 MB.
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L);

        // Set commit interval to 1 second.
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);

    }

    private static void startStream() {

        log.info("starting kafka streams");

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, Expense> expenseStream = builder.stream(INPUT_TOPIC_EXPENSE);

        final KTable<String, Integer> expensePerEmployeeTable = expenseStream

                // for demo purposes
                .peek((k, v) -> System.out.printf("new expense: %s\n", v.toString()))

                // set our wanted keys and values
                .map((k, v) -> new KeyValue<>(v.getEmployeeAcronym(), v.getAmount()))

                //KStream aggregations such as `reduce` operate on a per-key basis
                .groupByKey()
                .reduce((v1, v2) -> v1 + v2);

        expensePerEmployeeTable.toStream().to(OUTPUT_TOPIC_EMPLOYEE_EXPENSE);

        final KafkaStreams streamsContracts = new KafkaStreams(builder.build(), config);

        streamsContracts.cleanUp();
        streamsContracts.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streamsContracts::close));
    }
}