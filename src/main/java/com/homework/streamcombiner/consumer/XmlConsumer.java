package com.homework.streamcombiner.consumer;

import com.homework.streamcombiner.busobj.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.homework.streamcombiner.JAXBContextHolder.CONTEXT;

@Getter
@Slf4j
public class XmlConsumer implements Closeable {
    private static final AtomicInteger producerId = new AtomicInteger();


    private final Unmarshaller unmarshaller;
    private final XmlConsumerConfig config;
    private final BlockingQueue<Data> queue;
    private Socket socket;
    private Thread worker;
    private String name = "XmlConsumer-" + producerId.incrementAndGet();

    public XmlConsumer(XmlConsumerConfig config) {
        this.config = config;
        queue = new LinkedBlockingQueue<>(config.getQueueSize());
        socket = new Socket();
        try {
            unmarshaller = CONTEXT.createUnmarshaller();
        } catch (JAXBException e) {
            log.error("Could not create unmarshaller", e);
            throw new RuntimeException("Could not create unmarshaller", e);
        }
    }

    /**
     * Connect to socket with given {@link #config}#getHost and {@link #config}#getPort
     * @throws IOException - if an error occurs during the connection
     */
    public void connect() throws IOException {
        socket.connect(new InetSocketAddress(config.getHost(), config.getPort()));
    }

    /**
     * start Reading from socket to {@link #queue}
     */
    public void startRead() {
        if (socket.isConnected()) {
            worker = new Thread(() -> {
                try (InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                     BufferedReader din = new BufferedReader(inputStreamReader)) {
                    while (!worker.isInterrupted()) {
                        final String xml = din.readLine();
                        if (xml != null && !xml.isEmpty()) {
                            try {
                                final Data data = parseXml(xml);
                                if (data != null) {
                                    queue.put(data);
                                }
                            } catch (InterruptedException e) {
                                log.debug("Worker was interrupted while waiting for queue.put");
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error("Error reading from XmlProducer with config {}", config.toString(), e);
                }
            }, name);
            worker.start();
        } else {
            log.error("Socket is not connected");
        }
    }

    /**
     * Get next message from queue with given timeout
     * @param timeout how long to wait before giving up, in units of {@code unit}
     * @param unit - ta {@code TimeUnit} determining how to interpret the
     *        {@code timeout} parameter
     * @return the last message from queue, or {@code null} if the
     *       specified waiting time elapses before an element is available
     * @throws InterruptedException - if interrupted while waiting
     */
    public Data nextMessage(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    private Data parseXml(String xml) {
        try {
            return (Data) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            log.error("Error parsing xml to object \n'{}'", xml, e);
        }
        return null;
    }


    @Override
    public void close() {
        if (worker != null) {
            worker.interrupt();
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Error closing socket for xmlProducer '{}'", name, e);
            }
            log.info("Xml Producer '{}' stop was called", name);
        }
    }
}
