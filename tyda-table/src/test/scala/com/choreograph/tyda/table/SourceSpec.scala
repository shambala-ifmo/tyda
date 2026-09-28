package com.choreograph.tyda.table

import org.scalatest.funsuite.AnyFunSuite

class SourceSpec extends AnyFunSuite {
  private final case class Model(f: String)
  private final case class Date(date: Int)

  test("GraphDb source path is a stable graphdb:// identifier") {
    val source: Source[Model, Partitioner.None] = Source.GraphDb("graphdb.example.com", "my-repo")
    assert(source.path == "graphdb://graphdb.example.com/repositories/my-repo")
  }

  test("GraphDb source can not be read through Tyda's Dataset API") {
    val source: Source[Model, Partitioner.None] = Source.GraphDb("graphdb.example.com", "my-repo")
    intercept[UnsupportedOperationException](source.read)
  }

  test("GraphDb source can not be read partitioned through Tyda's Dataset API") {
    val source: Source[Model, Partitioner.Hive[Date]] = Source.GraphDb("graphdb.example.com", "my-repo")
    intercept[UnsupportedOperationException](source.asPartitionDataset(Partitioner.Hive.fromValue(Date(1))))
  }
}
