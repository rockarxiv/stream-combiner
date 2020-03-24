package com.homework.streamcombiner;


import com.homework.streamcombiner.busobj.Data;
import com.homework.streamcombiner.config.AppConfig;
import com.homework.streamcombiner.consumer.XmlConsumer;
import com.homework.streamcombiner.consumer.XmlConsumerConfig;
import com.homework.streamcombiner.producer.JsonProducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StreamCombinerTest {

    @Mock
    private XmlConsumer xmlConsumer1;

    @Mock
    private XmlConsumer xmlConsumer2;

    @Mock
    private JsonProducer producer;


    @Before
    public void init() {
        XmlConsumerConfig config = new XmlConsumerConfig();
        config.setHost("consumer1");
        when(xmlConsumer1.getConfig()).thenReturn(config);
        config = new XmlConsumerConfig();
        config.setHost("consumer2");
        when(xmlConsumer2.getConfig()).thenReturn(config);
    }


    @Test
    public void test2ConsumersWithSameTimestampData() throws Exception {
        Queue<Data> dataQueue1 = new LinkedList<>();
        dataQueue1.add(new Data(10, 10));
        Queue<Data> dataQueue2 = new LinkedList<>();
        dataQueue2.add(new Data(10, 3));
        final AppConfig config = createAppConfig();
        when(xmlConsumer1.nextMessage(config.getConsumerReadTimeout(), config.getConsumerReadTimeoutUnits())).thenAnswer(invocation -> dataQueue1.poll());
        when(xmlConsumer2.nextMessage(config.getConsumerReadTimeout(), config.getConsumerReadTimeoutUnits())).thenAnswer(invocation -> dataQueue2.poll());
        StreamCombiner.combineStreams(Arrays.asList(xmlConsumer1, xmlConsumer2), producer, config);
        ArgumentCaptor<Data> argument = ArgumentCaptor.forClass(Data.class);
        verify(producer).produce(argument.capture());
        assertEquals(10, argument.getValue().getTimestamp());
        assertEquals(13, argument.getValue().getAmount(), 0);
    }

    @Test
    public void test2ConsumersWithDifferentTimestampData() throws Exception {
        Queue<Data> dataQueue1 = new LinkedList<>();
        dataQueue1.add(new Data(10, 10));
        Queue<Data> dataQueue2 = new LinkedList<>();
        dataQueue2.add(new Data(12, 3));
        final AppConfig config = createAppConfig();
        when(xmlConsumer1.nextMessage(config.getConsumerReadTimeout(), config.getConsumerReadTimeoutUnits())).thenAnswer(invocation -> dataQueue1.poll());
        when(xmlConsumer2.nextMessage(config.getConsumerReadTimeout(), config.getConsumerReadTimeoutUnits())).thenAnswer(invocation -> dataQueue2.poll());
        StreamCombiner.combineStreams(Arrays.asList(xmlConsumer1, xmlConsumer2), producer, config);
        ArgumentCaptor<Data> argument = ArgumentCaptor.forClass(Data.class);
        verify(producer, times(2)).produce(argument.capture());
        final List<Data> allValues = argument.getAllValues();
        assertEquals(10, allValues.get(0).getTimestamp());
        assertEquals(10, allValues.get(0).getAmount(), 0);
        assertEquals(12, allValues.get(1).getTimestamp());
        assertEquals(3, allValues.get(1).getAmount(), 0);
    }

    private AppConfig createAppConfig() {
        final AppConfig config = new AppConfig();
        config.setConsumerReadTimeout(10);
        config.setConsumerReadTimeoutUnits(TimeUnit.SECONDS);
        return config;
    }

}
