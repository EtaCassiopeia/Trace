package org.apache.spark.metrics.source

import com.codahale.metrics.{Counter, Gauge, Meter, MetricRegistry}
import org.apache.spark.{Accumulator, SparkEnv}
import org.joda.time.DateTime

import scala.collection.mutable

/**
  * <h1>SparkInstrumentation</h1>
  * this class provides an Instrument to expose metrics
  *
  * @author Mohsen Zainalpour
  * @version 1.0
  * @since 10/6/15
  */

//has been borrowed from https://gist.github.com/ibuenros/9b94736c2bad2f4b8e23

/** Instrumentation for Spark based on accumulators.
  *
  * Usage:
  * val instrumentation = new SparkInstrumentation("example.metrics")
  * val numReqs = sc.accumulator(0L)
  * instrumentation.source.registerDailyAccumulator(numReqs, "numReqs")
  * instrumentation.register()
  *
  * Will create and report the following metrics:
  * - Gauge with total number of requests (daily)
  * - Meter with rate of requests
  *
  * @param prefix prefix for all metrics that will be reported by this Instrumentation
  */
class SparkInstrumentation(prefix: String) extends Serializable {
  val accumulators = mutable.Set[Accumulator[Long]]()

  class InstrumentationSource(prefix: String) extends Source {
    val metricRegistry = new MetricRegistry
    val sourceName = prefix

    val oldgauges = mutable.Map[String, Long]()
    val oldtimes = mutable.Map[String, DateTime]()
    val meters = mutable.Map[String, Meter]()
    val counters = mutable.Map[String, Counter]()

    /** Computes metrics based on accumulator. Gauge never resets.
      *
      * @param a    Metrics will be derived from this accumulator
      * @param name Name of the metrics
      */
    def registerAccumulator(a: Accumulator[Long], name: String) {
            oldgauges += (name -> 0l)
            meters += (name -> metricRegistry.meter(name + "-rate"))

            metricRegistry.register(MetricRegistry.name(name),
              new Gauge[Long] {
                override def getValue: Long = {
                  meters(name).mark((a.value - oldgauges(name)))
                  oldgauges(name) = a.value
                  return a.value
                }
              })
    }

    /** Computes metrics based on accumulator. Gauge never resets.
      *
      * @param a    Metrics will be derived from this accumulator
      * @param name Name of the metrics
      */
    def registerCounterAccumulator(a: Accumulator[Long], name: String) {
      counters += (name -> metricRegistry.counter(name + "-count"))
      oldgauges += (name -> 0l)

      metricRegistry.register(MetricRegistry.name(name), new Counter() {

        override def getCount: Long = {
          counters(name).inc((a.value - oldgauges(name)))
          oldgauges(name) = a.value
          a.value.longValue()
        }
      })
    }

    /** Computes metrics based on accumulator. Gauge resets at the end of the day.
      *
      * @param a    Metrics will be derived from this accumulator
      * @param name Name of the metrics
      */
    def registerDailyAccumulator(a: Accumulator[Long], name: String) {
      oldgauges += (name -> 0l)
      meters += (name -> metricRegistry.meter(name + "-rate"))
      oldtimes += (name -> DateTime.now)

      metricRegistry.register(MetricRegistry.name(name),
        new Gauge[Long] {
          override def getValue: Long = {
            meters(name).mark((a.value - oldgauges(name)))
            val now = DateTime.now
            if (now.getDayOfMonth != oldtimes(name).getDayOfMonth) {
              a.setValue(0l)
            }
            oldtimes(name) = now
            oldgauges(name) = a.value
            return a.value
          }
        })
    }
  }

  val source = new InstrumentationSource(prefix)

  /** Register the Instrumentation with Spark so the metrics are reported to any provided Sink. */
  def register() {
    SparkEnv.get.metricsSystem.registerSource(source)
  }
}
