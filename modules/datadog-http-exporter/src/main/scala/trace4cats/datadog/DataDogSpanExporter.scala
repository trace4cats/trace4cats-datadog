package trace4cats.datadog

import cats.Foldable
import cats.effect.kernel.Temporal
import cats.syntax.either._
import cats.syntax.functor._
import org.http4s.Method.PUT
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import trace4cats.HttpSpanExporter
import trace4cats.kernel.SpanExporter
import trace4cats.model.Batch

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
