package com.datorama.timbermill.pipe;

import com.datorama.timbermill.TaskIndexer;
import com.datorama.timbermill.unit.Event;
import org.elasticsearch.ElasticsearchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class LocalOutputPipe implements EventOutputPipe {

    private final BlockingQueue<Event> eventsQueue = new ArrayBlockingQueue<>(1000000);
    
    private TaskIndexer taskIndexer;
    private boolean keepRunning = true;
    private boolean stoppedRunning = false;

    private static final Logger LOG = LoggerFactory.getLogger(LocalOutputPipe.class);

    private LocalOutputPipe() {
    }

    private LocalOutputPipe(Builder builder) {
        if (builder.elasticUrl == null){
            throw new ElasticsearchException("Must enclose an Elasticsearch URL");
        }
        taskIndexer = new TaskIndexer(builder.pluginsJson, builder.propertiesLengthMap,
                builder.defaultMaxChars, builder.elasticUrl, builder.daysRotation, builder.awsRegion,
                builder.indexBulkSize, builder.indexingThreads, builder.elasticUser, builder.elasticPassword, builder.maxCacheSize, builder.maxCacheHoldTimeMinutes);

        getWorkingThread().start();
    }

    private Thread getWorkingThread() {
        return new Thread(() -> {
            while (keepRunning) {
                while(!eventsQueue.isEmpty()) {
                    try{
                        Collection<Event> events = new ArrayList<>();
                        eventsQueue.drainTo(events);
                        Map<String, List<Event>> eventsPerEnvMap = events.stream().collect(Collectors.groupingBy(event -> event.getEnv()));
                        for (Map.Entry<String, List<Event>> eventsPerEnv : eventsPerEnvMap.entrySet()) {
                            String env = eventsPerEnv.getKey();
                            Collection<Event> currentEvents = eventsPerEnv.getValue();
                            taskIndexer.retrieveAndIndex(currentEvents, env);
                        }
                        Thread.sleep(2000);
                    } catch (RuntimeException | InterruptedException e) {
                        LOG.error("Error was thrown from TaskIndexer:", e);
                    }
                }
            }
            stoppedRunning = true;
        });
    }

    @Override
    public void send(Event e){
        eventsQueue.add(e);
    }

    public void close() {
        taskIndexer.close();
        keepRunning = false;
        while(!stoppedRunning){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static class Builder {

        private int maxCacheHoldTimeMinutes = 60;
        private int maxCacheSize = 10000;
        private String elasticUrl = null;
        private String pluginsJson = "[]";
        private Map<String, Integer> propertiesLengthMap = Collections.EMPTY_MAP;
        private int defaultMaxChars = 1000000;
        private int daysRotation = 0;
        private int indexBulkSize = 2097152;
        private int indexingThreads = 1;
        private String elasticUser;
        private String awsRegion;
        private String elasticPassword;

        public LocalOutputPipe.Builder url(String elasticUrl) {
            this.elasticUrl = elasticUrl;
            return this;
        }

        public LocalOutputPipe.Builder pluginsJson(String pluginsJson) {
            this.pluginsJson = pluginsJson;
            return this;
        }

        public LocalOutputPipe.Builder propertiesLengthMap(Map<String, Integer> propertiesLengthMap) {
            this.propertiesLengthMap = propertiesLengthMap;
            return this;
        }

        public LocalOutputPipe.Builder defaultMaxChars(int defaultMaxChars) {
            this.defaultMaxChars = defaultMaxChars;
            return this;
        }

        public LocalOutputPipe.Builder daysRotation(int daysRotation) {
            this.daysRotation = daysRotation;
            return this;
        }

        public LocalOutputPipe.Builder awsRegion(String awsRegion) {
            this.awsRegion = awsRegion;
            return this;
        }

        public LocalOutputPipe.Builder indexBulkSize(int indexBulkSize) {
            this.indexBulkSize = indexBulkSize;
            return this;
        }

        public LocalOutputPipe.Builder indexingThreads(int indexingThreads) {
            this.indexingThreads = indexingThreads;
            return this;
        }

        public LocalOutputPipe.Builder elasticUser(String elasticUser) {
            this.elasticUser = elasticUser;
            return this;
        }

        public LocalOutputPipe.Builder elasticPassword(String elasticPassword) {
            this.elasticPassword = elasticPassword;
            return this;
        }

        public LocalOutputPipe.Builder maxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        public LocalOutputPipe.Builder maxCacheHoldTimeMinutes(int maxCacheHoldTimeMinutes) {
            this.maxCacheHoldTimeMinutes = maxCacheHoldTimeMinutes;
            return this;
        }

        public LocalOutputPipe build() {
            return new LocalOutputPipe(this);
        }
    }
}
