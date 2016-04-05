package org.github.etacassiopeia.trace;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.github.etacassiopeia.trace.kafka.Kafka;
import com.twitter.chill.KryoInstantiator;
import com.twitter.chill.KryoPool;
import opentracing.Span;
import opentracing.Tracer;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.gracefulStop;

/**
 * <h1>DefaultTracer</h1>
 * The DefaultTracer
 *
 * @author Mohsen Zainalpour
 * @version 1.0
 * @since 12/03/16
 */
public class DefaultTracer implements Tracer {

    private final ActorSystem system;
    private final ActorRef mgr;

    private int POOL_SIZE = 10;
    private KryoPool kryo = KryoPool.withByteArrayOutputStream(POOL_SIZE, new KryoInstantiator());

    public DefaultTracer() {
        system = ActorSystem.apply("KafkaSpanStoreSystem");
        mgr = Kafka.get(system).manager();
    }

    @Override
    public SpanBuilder buildSpan() {
        return new DefaultSpanBuilder(this);
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new DefaultSpanBuilder(this).withOperationName(operationName);
    }

    @Override
    public void close() {
        Future<Boolean> stopped = gracefulStop(mgr, Duration.create(5, TimeUnit.SECONDS), system);
        try {
            Await.result(stopped, Duration.create(6, TimeUnit.SECONDS));
        } catch (Exception e) {
        }

        system.shutdown();
        system.awaitTermination();
    }

    @Override
    public void persist(Span span) {
        byte[] ser = kryo.toBytesWithClass(span);
        mgr.tell(new Kafka.SpanMessage(ser), ActorRef.noSender());
    }

    static class DefaultSpanBuilder implements Tracer.SpanBuilder {

        private Tracer tracer;

        private String operationName = null;
        private Span parent = null;
        Map<String, String> tags = new HashMap<>();

        DefaultSpanBuilder(Tracer tracer) {
            this.tracer = tracer;
        }

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

            Span span = new SimpleSpan(tracer, operationNameBuilder.toString(), parent != null ? parent.getId() : null, startTimestamp);
            for (Map.Entry<String, String> entry : tags.entrySet())
                span.setTag(entry.getKey(), entry.getValue());

            return span;
        }

    }
}
