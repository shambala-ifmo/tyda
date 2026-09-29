package com.choreograph.tyda.collection

import scala.reflect.ClassTag
import scala.collection.AbstractSeq
import scala.collection.IndexedSeqOps
import scala.collection.StrictOptimizedSeqOps
import scala.collection.Factory
import scala.collection.mutable.Builder
import com.choreograph.tyda.Ord
import com.choreograph.tyda.Arbitrary
import com.choreograph.tyda.Codec
import com.choreograph.tyda.Injection

final class StrictArray[T] private (private val elems: scala.Array[T])
    extends AbstractSeq[T],
      IndexedSeq[T],
      IndexedSeqOps[T, IndexedSeq, IndexedSeq[T]],
      StrictOptimizedSeqOps[T, IndexedSeq, IndexedSeq[T]],
      Serializable {
  def apply(i: Int): T = elems(i)
  def length: Int = elems.length

  /** Zero-copy view of the backing array. Do not mutate. */
  def unsafeArray: scala.Array[T] = elems

  override def className: String = "StrictArray"
}

object StrictArray {
  def apply[T: ClassTag](elems: T*): StrictArray[T] = new StrictArray(elems.toArray)
  def from[T: ClassTag](it: IterableOnce[T]): StrictArray[T] = new StrictArray(scala.Array.from(it))
  def unsafeWrapArray[T](array: scala.Array[T]): StrictArray[T] = new StrictArray(array)
  def empty[T: ClassTag]: StrictArray[T] = new StrictArray(scala.Array.empty[T])

  given factory[T: ClassTag]: Factory[T, StrictArray[T]] = new Factory[T, StrictArray[T]] {

    override def fromSpecific(it: IterableOnce[T]): StrictArray[T] = StrictArray.from(it)

    def newBuilder: Builder[T, StrictArray[T]] = scala.Array.newBuilder[T].mapResult(unsafeWrapArray)
  }

  given ord[T: Ord]: Ord[StrictArray[T]] = Ord.seq
  given arbitrary[T: Arbitrary: ClassTag]: Arbitrary[StrictArray[T]] = Arbitrary.iterable

  private final class StrictArrayInjection[T] extends Injection[StrictArray[T], scala.Array[T]] {
    def apply(from: StrictArray[T]): scala.Array[T] = from.unsafeArray
    def invert(to: scala.Array[T]): StrictArray[T] = StrictArray.unsafeWrapArray(to)
  }
  
  given codec[T](using elementCodec: Codec[T]): Codec[StrictArray[T]] =
    Codec.fromInjection(new StrictArrayInjection, Codec.array[T])

}