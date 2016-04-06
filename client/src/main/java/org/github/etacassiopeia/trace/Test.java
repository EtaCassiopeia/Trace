package org.github.etacassiopeia.trace;

import opentracing.Span;
import opentracing.Tracer;

/**
 * <h1>Test</h1>
 * The Test
 *
 * @author Mohsen Zainalpour
 * @version 1.0
 * @since 12/03/16
 */
public class Test {
    public static void main(String[] args) throws Exception {
        Tracer tracer = new DefaultTracer();

        Span test = tracer.buildSpan().withTag("key1", "value1").withOperationName("fetcher").start();
        test.finish();

        System.out.println(test);

        tracer.close();
    }
}
