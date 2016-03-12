package org.github.etacassiopeia.trace;

import opentracing.Span;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <h1>SimpleSpan</h1>
 * The SimpleSpan
 *
 * @author Mohsen Zainalpour
 * @version 1.0
 * @since 12/03/16
 */
public class SimpleSpan implements Span, Serializable {

    private String operationName;
    private UUID spanId;
    private UUID parentSpanId = null;
    private long startTimestamp;
    private long finishTimestamp;

    private Map<String, String> tags = new HashMap<>();
    private Map<String, String> bags = new HashMap<>();
    private List<Log> logs = new ArrayList<>();

    public SimpleSpan(String operationName) {
        this(operationName, null);
    }

    public SimpleSpan(String operationName, UUID parentSpanId) {
        this(operationName, parentSpanId, System.currentTimeMillis());
    }

    public SimpleSpan(String operationName, UUID parentSpanId, long startTimestamp) {
        spanId = java.util.UUID.randomUUID();
        this.operationName = operationName;
        this.parentSpanId = parentSpanId;
        this.startTimestamp = startTimestamp;
    }

    @Override
    public void finish() {
        this.finishTimestamp = System.currentTimeMillis();
        //TODO replace this temporary message with real implementation
        System.out.println("finished");
    }

    @Override
    public UUID getId() {
        return spanId;
    }

    @Override
    public boolean isRootSpan() {
        return parentSpanId != null;
    }

    @Override
    public Span setTag(String key, String value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public Span setBaggageItem(String key, String value) {
        bags.put(key, value);
        return this;
    }

    @Override
    public String getBaggageItem(String key) {
        return bags.getOrDefault(key, "");
    }

    @Override
    public Span log(String eventName, Object payload) {
        logs.add(new Log(eventName, payload));
        return this;
    }

    @Override
    public Span log(long timestampMicroseconds, String eventName, Object payload) {
        logs.add(new Log(timestampMicroseconds, eventName, payload));
        return this;
    }

    public static class Log implements Serializable {
        private long timestamp;
        private String eventName;
        private Object payload;

        public Log(String eventName, Object payload) {
            this(System.currentTimeMillis(), eventName, payload);
        }

        public Log(long timestamp, String eventName, Object payload) {
            this.timestamp = timestamp;
            this.eventName = eventName;
            this.payload = payload;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getEventName() {
            return eventName;
        }

        public Object getPayload() {
            return payload;
        }
    }

    @Override
    public String toString() {
        return "SimpleSpan{" +
                "operationName='" + operationName + '\'' +
                ", spanId=" + spanId +
                ", parentSpanId=" + parentSpanId +
                ", startTimestamp=" + startTimestamp +
                ", finishTimestamp=" + finishTimestamp +
                ", tags=" + tags +
                ", bags=" + bags +
                ", logs=" + logs +
                '}';
    }
}
