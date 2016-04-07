/**
 * Copyright 2016 The OpenTracing Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package opentracing;


/**
 * Tracer is a simple, thin interface for Span creation, and Span propagation into different transport formats.
 */
public interface Tracer {

    /**
     * Create, start, and return a new Span with the given `operationName`.
     * An optional parent Span can be specified used to incorporate the newly-returned Span into an existing trace.
     * <p>
     * <p>Example:
     * <pre>{@code
     * Tracer tracer = ...
     *
     * Span feed = tracer.buildSpan()
     * .start();
     *
     * Span http = tracer.buildSpan()
     * .withParent(feed)
     * .withTag("user_agent", req.UserAgent)
     * .withTag("lucky_number", 42)
     * .start();
     * }</pre>
     */
    SpanBuilder buildSpan();

    /**
     * Create, start, and return a new Span with the given `operationName`.
     * An optional parent Span can be specified used to incorporate the newly-returned Span into an existing trace.
     * <p>
     * <p>Example:
     * <pre>{@code
     * Tracer tracer = ...
     *
     * Span feed = tracer.buildSpan("GetFeed")
     * .start();
     *
     * Span http = tracer.buildSpan("HandleHTTPRequest")
     * .withParent(feed)
     * .withTag("user_agent", req.UserAgent)
     * .withTag("lucky_number", 42)
     * .start();
     * }</pre>
     */
    SpanBuilder buildSpan(String operationName);

    /** Takes two arguments:
     *    a Span instance, and
     *    a “carrier” object in which to inject that Span for cross-process propagation.
     *
     * A “carrier” object is some sort of http or rpc envelope, for example HeaderGroup (from Apache HttpComponents).
     *
     * Attempting to inject to a carrier that has been registered/configured to this Tracer will result in a
     * IllegalStateException.
     */
    //<T> void inject(Span span, T carrier);

    /**
     * Returns a SpanBuilder provided
     * a “carrier” object from which to extract identifying information needed by the new Span instance.
     * <p>
     * If the carrier object has no such span stored within it, a new Span is created.
     * <p>
     * Unless there’s an error, it returns a SpanBuilder.
     * The Span generated from the builder can be used in the host process like any other.
     * <p>
     * (Note that some OpenTracing implementations consider the Spans on either side of an RPC to have the same identity,
     * and others consider the caller to be the parent and the receiver to be the child)
     * <p>
     * Attempting to join from a carrier that has been registered/configured to this Tracer will result in a
     * IllegalStateException.
     * <p>
     * If the span serialized state is invalid (corrupt, wrong version, etc) inside the carrier this will result in a
     * IllegalArgumentException.
     */
    // <T> SpanBuilder join(T carrier);


    //Close Tracer and release resources
    void close() throws Exception;

    void persist(Span span);

    interface SpanBuilder {

        /**
         * Specify the operationName.
         * <p>
         * If the operationName has already been set (implicitly or explicitly) an IllegalStateException will be thrown.
         */
        SpanBuilder withOperationName(String operationName);

        /**
         * Specify the parent span
         * <p>
         * If the parent has already been set an IllegalStateException will be thrown.
         */
        SpanBuilder withParent(Span parent);

        /**
         * Same as {@link Span#setTag(String, String)}, but for the span being built.
         */
        SpanBuilder withTag(String key, String value);


        /**
         * Returns the started Span.
         */
        Span start();

        /**
         * Returns the Span, with a started timestamp (represented in microseconds) as specified.
         */
        Span start(long microseconds);

    }
}