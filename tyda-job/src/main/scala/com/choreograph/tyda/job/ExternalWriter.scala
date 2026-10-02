package com.choreograph.tyda.job

trait ExternalWriter {
  /** Writes a single document to the external resource identified by `uri`. */
  def write(uri: String, document: String): Unit
}

object ExternalWriter {
  val unimplemented: ExternalWriter = (uri, _) =>
    throw new UnsupportedOperationException(
      s"No ExternalWriter configured for $uri; override TydaJob.externalWriter to enable writing."
    )
}
