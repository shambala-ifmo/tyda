package com.choreograph.tyda.job

trait ExternalReader {
  /** Reads all documents available at the external resource identified by `uri`. */
  def read(uri: String): Seq[String]
}

object ExternalReader {
  val unimplemented: ExternalReader = uri =>
    throw new UnsupportedOperationException(
      s"No ExternalReader configured for $uri; override TydaJob.externalReader to enable reading."
    )
}
