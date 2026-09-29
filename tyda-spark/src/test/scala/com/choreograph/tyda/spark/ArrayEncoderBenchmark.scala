package com.choreograph.tyda.spark

import scala.util.Random

import com.choreograph.tyda.Codec
import scala.collection.immutable.ArraySeq
import com.choreograph.tyda.collection.StrictArray

/** Quick throwaway microbenchmark comparing the Codec.Array[T] primitive fast path against the
  * pre-existing generic Seq[T] path for the same embedding data, at the codec (serializer/deserializer)
  * level, bypassing SparkSession/query planning entirely.
  *
  * Run with:
  *   sbt "tydaSpark3/Test/runMain com.choreograph.tyda.spark.ArrayEncoderBenchmark"
  *   sbt "tydaSpark4/Test/runMain com.choreograph.tyda.spark.ArrayEncoderBenchmark"
  */
object ArrayEncoderBenchmark {
  private final case class WithStrictArray(embedding: StrictArray[Float]) derives Codec
  private final case class WithArraySeq(embedding: ArraySeq[Float]) derives Codec


  private def bench[T: Codec](name: String, rows: IndexedSeq[T], warmupRounds: Int, timedRounds: Int): Unit = {
    val encoder = CodecToEncoder.convertInternal[T].resolveAndBind()
    val serializer = encoder.createSerializer()
    val deserializer = encoder.createDeserializer()

    def runOnce(): (Long, Long) = {
      val serStart = System.nanoTime()
      val serialized = rows.map(row => serializer(row).copy())
      val serEnd = System.nanoTime()
      val deserialized = serialized.map(deserializer)
      val deserEnd = System.nanoTime()
      assert(deserialized.size == rows.size)
      (serEnd - serStart, deserEnd - serEnd)
    }

    (1 to warmupRounds).foreach(_ => runOnce())

    val results = (1 to timedRounds).map(_ => runOnce())
    val avgSerNs = results.map(_._1).sum / timedRounds
    val avgDeserNs = results.map(_._2).sum / timedRounds
    val n = rows.size

    println(
      f"$name%-28s serialize ${avgSerNs / 1e6}%8.2f ms (${avgSerNs.toDouble / n}%7.1f ns/row)" +
        f"   deserialize ${avgDeserNs / 1e6}%8.2f ms (${avgDeserNs.toDouble / n}%7.1f ns/row)"
    )
  }

  def main(args: Array[String]): Unit = {
    val rng = new Random(42)
    val dim = 768
    val n = 100000

    val arrayRows = IndexedSeq.fill(n)(WithStrictArray(StrictArray.unsafeWrapArray(Array.fill(dim)(rng.nextFloat()))))
    val seqRows = IndexedSeq.fill(n)(WithArraySeq(ArraySeq.fill(dim)(rng.nextFloat())))

    println(s"rows=$n, embedding dim=$dim, spark = ${org.apache.spark.SPARK_VERSION}")
    bench("StrictArray[Float] (fast path)", arrayRows, warmupRounds = 5, timedRounds = 10)
    bench("ArraySeq[Float] (generic path)", seqRows, warmupRounds = 5, timedRounds = 10)
  }
}
