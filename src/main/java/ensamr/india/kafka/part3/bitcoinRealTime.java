package ensamr.india.kafka.part3;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import javax.swing.*;
import java.time.Duration;
import java.util.*;

 class bitcoinRealTime {
    MySwingWorker mySwingWorker;
    SwingWrapper<XYChart> sw;
    XYChart chart;

    public static void main(String[] args) throws Exception {

        bitcoinRealTime btc = new bitcoinRealTime();
        btc.go();
    }

    private void go() {
        chart = QuickChart.getChart("Prix de BTC en Temps réel", "Temps", "Valeur en DH", "randomWalk", new double[] { 0 }, new double[] { 0 });
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisTicksVisible(false);
        sw = new SwingWrapper<XYChart>(chart);
        sw.displayChart();
        mySwingWorker = new MySwingWorker();
        mySwingWorker.execute();
    }

    private class MySwingWorker extends SwingWorker<Boolean, double[]> {
        LinkedList<Double> fifo = new LinkedList<Double>();
        public MySwingWorker() {

        }

        @Override
        protected Boolean doInBackground() throws Exception {
            ///
            Properties kafkaProps = new Properties();
            kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    "localhost:9092");
            kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringDeserializer");
            kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringDeserializer");
            kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG,
                    "kafka-java-consumer");

            KafkaConsumer<String, String> simpleConsumer =
                    new KafkaConsumer<String,String>(kafkaProps);

            simpleConsumer.subscribe(Arrays.asList("kafka.ensamr.bitcoin.mad"));
            ///

            while (!isCancelled()) {
                Random random = new Random();

                ConsumerRecords<String, String> messages =
                        simpleConsumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> message : messages)
                {
                    //Double ds = Double.valueOf(message.value()) / 10;
                        fifo.add(Double.valueOf(message.value()));
                }
                if (fifo.size() > 500) {
                    fifo.removeFirst();
                }

                double[] array = new double[fifo.size()];
                for (int i = 0; i < fifo.size(); i++) {
                    array[i] = fifo.get(i);
                }
                publish(array);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // eat it. caught when interrupt is called
                    System.out.println("MySwingWorker shut down.");
                }

            }
            return true;
        }

        @Override
        protected void process(List<double[]> chunks) {

            //System.out.println("number of chunks: " + chunks.size());

            double[] mostRecentDataSet = chunks.get(chunks.size() - 1);

            chart.updateXYSeries("randomWalk", null, mostRecentDataSet, null);
            sw.repaintChart();

            long start = System.currentTimeMillis();
            long duration = System.currentTimeMillis() - start;
            try {
                Thread.sleep(20 - duration); // 40 ms ==> 25fps
                // Thread.sleep(400 - duration); // 40 ms ==> 2.5fps
            } catch (InterruptedException e) {
            }

        }
    }
}