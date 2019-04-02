package com.datorama.timbermill.pipe;

import com.datorama.timbermill.ElasticsearchClient;
import com.datorama.timbermill.unit.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class TimbermillOutputPipe implements EventOutputPipe {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchClient.class);

    ArrayBlockingQueue<Event> queue = new ArrayBlockingQueue<Event>(Integer.MAX_VALUE);

//    public TimbermillOutputPipe(TimbermillOutputPipeConfig config) {
//
//    }

    @Override
    public void send(Event e) {
        queue.offer(e);
    }

    @Override
    public void close() {
    }

    @Override
    public Map<String, String> getStaticParams() {
        return null;
    }

    public void start() {

    }

}