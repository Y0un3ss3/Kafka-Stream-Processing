package ensamr.india.kafka.part3;

import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class priceProducer {
    public static void main(String[] args) throws AblyException {
        Properties kafkaProps = new Properties();

        //List of brokers to connect to
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer priceProducer = new KafkaProducer(kafkaProps);


        AblyRealtime realtime = new AblyRealtime("[YOUR_API]");
        String chanName = "[product:ably-coindesk/bitcoin]bitcoin:usd";
        Channel channel = realtime.channels.get(chanName);
        channel.subscribe(new Channel.MessageListener() {
            @Override
            public void onMessage(Message message) {
                ProducerRecord<String,String> kafkaRecord =
                        new ProducerRecord<String,String>(
                                "kafka.ensamr.bitcoin.usd",    //Topic name
                                "btcPrice",                          //Key for the message
                                (String) message.data                //Message Content
                        );
                priceProducer.send(kafkaRecord);
            }
        });

    }

}
