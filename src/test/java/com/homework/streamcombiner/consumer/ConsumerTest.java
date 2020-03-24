package com.homework.streamcombiner.consumer;
import com.homework.streamcombiner.busobj.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerTest {

    @Mock
    private Socket socket;

    @Test
    public void testConsumerHung() throws InterruptedException {
        final XmlConsumerConfig config = new XmlConsumerConfig();
        config.setQueueSize(10);
        XmlConsumer xmlConsumer = new XmlConsumer(config);
        final Data data = xmlConsumer.nextMessage(1, TimeUnit.SECONDS);
        assertNull("Data must be null", data);
    }

    @Test
    public void testConsumer() throws Exception {
        final XmlConsumerConfig config = new XmlConsumerConfig();
        config.setQueueSize(10);
        config.setHost("host");
        config.setPort(9999);
        try(XmlConsumer xmlConsumer = new XmlConsumer(config)) {
            Whitebox.setInternalState(xmlConsumer, "socket", socket);
            String consumerData =
                    "<data> <timestamp>1</timestamp> <amount>1</amount> </data>\n" +
                            "<data> <timestamp>2</timestamp> <amount>2.2</amount> </data>\n" +
                            "<data> <timestamp>4</timestamp> <amount>3.2</amount> </data>\n";
            List<Data> expected = Arrays.asList(new Data(1, 1),
                    new Data(2, (float) 2.2),
                    new Data(4, (float) 3.2)
            );
            when(socket.isConnected()).thenReturn(true);
            when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(consumerData.getBytes()));
            xmlConsumer.connect();
            xmlConsumer.startRead();
            for (Data value : expected) {
                final Data data = xmlConsumer.nextMessage(1, TimeUnit.SECONDS);
                assertEquals(value, data);
            }
        }
    }

}
