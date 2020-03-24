package com.homework.streamcombiner.config;

import com.homework.streamcombiner.consumer.XmlConsumerConfig;
import lombok.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class AppConfig {
    private int consumerReadTimeout;
    private TimeUnit consumerReadTimeoutUnits;
    private List<XmlConsumerConfig> xmlConsumerConfigs;
}
