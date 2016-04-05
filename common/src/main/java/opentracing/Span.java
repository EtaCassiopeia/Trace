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

import java.util.UUID;

/**
 * Represents an in-flight span in the opentracing system.
 * <p>
 * <p>Spans are created by the {@link Tracer#buildSpan} interface.
 */
public interface Span {

    /**
     * Sets the end timestamp and records the span.
     * <p>
     * <p>This should be the last call made to any span instance, and to do otherwise leads to
     * undefined behavior.
     */
    void finish();

    UUID getId();

    boolean isRootSpan();

    /**
     * Set a key:value tag on the Span.
     */
    Span setTag(String key, String value);

    /**
     * Set a Baggage item, represented as a simple string:string pair.
     * <p>
     * Note that newly-set Baggage items are only guaranteed to propagate to future children of the given Span.
     */
    Span setBaggageItem(String key, String value);

    /**
     * Get a Baggage item by key.
     * <p>
     * Returns null if no entry found, or baggage is not supported in the current implementation.
     */
    String getBaggageItem(String key);

    /**
     * Add a new log event to the Span, accepting an event name string and an optional structured payload argument.
     * If specified, the payload argument may be of any type and arbitrary size,
     * though implementations are not required to retain all payload arguments
     * (or even all parts of all payload arguments).
     * <p>
     * The timestamp of this log event is the current time.
     **/
    Span log(String eventName, /* @Nullable */ Object payload);

    /**
     * Add a new log event to the Span, accepting an event name string and an optional structured payload argument.
     * If specified, the payload argument may be of any type and arbitrary size,
     * though implementations are not required to retain all payload arguments
     * (or even all parts of all payload arguments).
     * <p>
     * The timestamp is specified manually here to represent a past log event.
     * The timestamp in microseconds in UTC time.
     **/
    Span log(long timestampMicroseconds, String eventName, /* @Nullable */ Object payload);
}