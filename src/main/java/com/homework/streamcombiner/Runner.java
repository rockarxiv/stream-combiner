package com.homework.streamcombiner;

import com.homework.streamcombiner.config.AppConfig;
import com.homework.streamcombiner.consumer.XmlConsumer;
import com.homework.streamcombiner.consumer.XmlConsumerConfig;
import com.homework.streamcombiner.producer.JsonProducer;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Runner {

    public static void main(String... args) throws Exception {
        if (args.length == 0) {
            log.error("Config file path is missing in arguments");
            throw new IllegalArgumentException("Config file path is missing in arguments");
        }
        final Constructor constructor = new Constructor(AppConfig.class);
        final TypeDescription typeDescription = new TypeDescription(AppConfig.class);
        typeDescription.addPropertyParameters("xmlProducerConfigs", XmlConsumerConfig.class);
        constructor.addTypeDescription(typeDescription);
        Yaml yaml = new Yaml(constructor);
        try(FileInputStream inputStream = new FileInputStream(args[0])) {
            final AppConfig config = yaml.load(inputStream);
            log.info("Running StreamCombiner with config '{}'", config.toString());
            JsonProducer producer = new JsonProducer();
            List<XmlConsumer> consumers = config.getXmlConsumerConfigs().stream().parallel().map(XmlConsumer::new)
                    .filter(xmlConsumer -> {
                        try {
                            xmlConsumer.connect();
                            xmlConsumer.startRead();
                            return true;
                        } catch (IOException e) {
                            log.error("Error connecting to xmlConsumer with config '{}'", xmlConsumer.getConfig(), e);
                            return false;
                        }
                    }).collect(Collectors.toList());
            StreamCombiner.combineStreams(consumers, producer, config);
        } catch (IOException e) {
            log.error("Error reading appConfig from '{}'", args[0], e);
            throw e;
        }
    }
}
