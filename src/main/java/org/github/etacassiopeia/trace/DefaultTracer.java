package org.github.etacassiopeia.trace;

import opentracing.Span;
import opentracing.Tracer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * <h1>DefaultTracer</h1>
 * The DefaultTracer
 *
 * @author Mohsen Zainalpour
 * @version 1.0
 * @since 12/03/16
 */
public class DefaultTracer implements Tracer {
    @Override
    public SpanBuilder buildSpan() {
        return new DefaultSpanBuilder();
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new DefaultSpanBuilder().withOperationName(operationName);
    }

    static class DefaultSpanBuilder implements Tracer.SpanBuilder {

        private String operationName = null;
        private Span parent = null;
        Map<String, String> tags = new HashMap<>();

        @Override
        public SpanBuilder withOperationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        @Override
        public SpanBuilder withParent(Span parent) {
            this.parent = parent;
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, String value) {
            tags.put(key, value);
            return this;
        }

        @Override
        public Span start() {
            return createSpan(System.currentTimeMillis());
        }

        @Override
        public Span start(long timestamp) {
            return createSpan(timestamp);
        }

        private Span createSpan(Long startTimestamp) {

            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            StackTraceElement sti = stackTraceElements[3];

            StringBuilder operationNameBuilder = new StringBuilder();
            operationNameBuilder.append(sti.getClassName()).append(":").append(sti.getMethodName());

            if (operationName != null && !operationName.isEmpty())
                operationNameBuilder.append("-").append(operationName);

            try {
                String hostAddress = InetAddress.getLocalHost().getHostAddress();
                operationNameBuilder.append("@").append(hostAddress);
            } catch (UnknownHostException e) {
                //TODO add log
            }

            Span span = new SimpleSpan(operationNameBuilder.toString(), parent != null ? parent.getId() : null, startTimestamp);
            for (Map.Entry<String, String> entry : tags.entrySet())
                span.setTag(entry.getKey(), entry.getValue());

            return span;
        }

    }
}
