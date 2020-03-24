package com.homework.streamcombiner.consumer;

import lombok.*;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class XmlConsumerConfig {
    private String host;
    private int port;
    private int queueSize;
}
