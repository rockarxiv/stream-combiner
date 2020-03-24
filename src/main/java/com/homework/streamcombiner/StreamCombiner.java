package com.homework.streamcombiner;

import com.homework.streamcombiner.busobj.Data;
import com.homework.streamcombiner.config.AppConfig;
import com.homework.streamcombiner.consumer.XmlConsumer;
import com.homework.streamcombiner.producer.JsonProducer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class StreamCombiner {


    /**
     * Combine given consumer streams and send to given producer
     *
     * @param consumers - consumers stream to combine
     * @param producer  - producer to write a result
     * @param config    - application config
     */
    public static void combineStreams(@NonNull List<XmlConsumer> consumers, @NonNull JsonProducer producer, @NonNull AppConfig config) {
        try {
            List<XmlConsumer> consumersToRead = new ArrayList<>(consumers);
            Map<Long, DataEntry> dataEntries = new TreeMap<>();
            boolean isRunning = true;
            while (isRunning) {
                if (consumersToRead.isEmpty() && dataEntries.isEmpty()) {
                    log.info("No Data left, stopping StreamCombiner");
                    isRunning = false;
                } else {
                    consumersToRead.forEach(xmlConsumer -> {
                        try {
                            final Data data = xmlConsumer.nextMessage(config.getConsumerReadTimeout(), config.getConsumerReadTimeoutUnits());
                            if (data == null) {
                                log.error("xmlConsumer with config '{}' not available for more then {} {} and will be removed", xmlConsumer.getConfig().toString(), config.getConsumerReadTimeout(), config.getConsumerReadTimeoutUnits());
                                xmlConsumer.close();
                            } else {
                                final DataEntry dataEntry = dataEntries.computeIfAbsent(data.getTimestamp(), timestamp -> new DataEntry(new Data(timestamp, 0)));
                                dataEntry.xmlConsumers.add(xmlConsumer);
                                dataEntry.data.mergeData(data);
                            }
                        } catch (InterruptedException e) {
                            log.error("StreamCombiner was Interrupted while waiting for message from xmlConsumer", e);
                        }
                    });
                    consumersToRead.clear();
                    final Iterator<DataEntry> iterator = dataEntries.values().iterator();
                    DataEntry dataEntry;
                    if (iterator.hasNext()) {
                        dataEntry = iterator.next();
                        dataEntries.remove(dataEntry.data.getTimestamp());
                        consumersToRead.addAll(dataEntry.xmlConsumers);
                        producer.produce(dataEntry.data);
                    }
                }
            }
        } finally {
            consumers.forEach(XmlConsumer::close);
        }
    }

    @RequiredArgsConstructor
    private static class DataEntry {
        private final Data data;
        private List<XmlConsumer> xmlConsumers = new ArrayList<>();

    }

}
