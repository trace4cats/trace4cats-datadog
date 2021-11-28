package io.janstenpickle.trace4cats.datadog

import cats.Foldable
import cats.effect.kernel.Temporal
import cats.syntax.either._
import cats.syntax.functor._
import io.janstenpickle.trace4cats.`export`.HttpSpanExporter
import io.janstenpickle.trace4cats.kernel.SpanExporter
import io.janstenpickle.trace4cats.model.Batch
import org.http4s.Method.PUT
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client

object DataDogSpanExporter {

  def apply[F[_]: Temporal, G[_]: Foldable](client: Client[F], host: String, port: Int): F[SpanExporter[F, G]] =
    Uri.fromString(s"http://$host:$port/v0.3/traces").liftTo[F].map { uri =>
      HttpSpanExporter[F, G, List[List[DataDogSpan]]](
        client,
        uri,
        (batch: Batch[G]) => DataDogSpan.fromBatch(batch),
        PUT
      )
    }

  def apply[F[_]: Temporal, G[_]: Foldable](client: Client[F], uri: Uri): SpanExporter[F, G] =
    HttpSpanExporter[F, G, List[List[DataDogSpan]]](client, uri, (batch: Batch[G]) => DataDogSpan.fromBatch(batch), PUT)
}
