package trace4cats.datadog

import cats.effect.kernel.{Async, Resource}
import fs2.Chunk
import org.http4s.Uri
import org.http4s.client.Client
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import trace4cats.kernel.SpanCompleter
import trace4cats.model.TraceProcess
import trace4cats.{CompleterConfig, QueuedSpanCompleter}

object DataDogSpanCompleter {

  def apply[F[_]: Async](
    client: Client[F],
    process: TraceProcess,
    host: String = "localhost",
    port: Int = 8126,
    config: CompleterConfig = CompleterConfig()
  ): Resource[F, SpanCompleter[F]] =
    Resource.eval(Slf4jLogger.create[F]).flatMap { implicit logger: Logger[F] =>
      Resource
        .eval(DataDogSpanExporter[F, Chunk](client, host, port))
        .flatMap(QueuedSpanCompleter[F](process, _, config))
    }

  def apply[F[_]: Async](
    client: Client[F],
    process: TraceProcess,
    uri: Uri,
    config: CompleterConfig
  ): Resource[F, SpanCompleter[F]] =
    Resource.eval(Slf4jLogger.create[F]).flatMap { implicit logger: Logger[F] =>
      QueuedSpanCompleter[F](process, DataDogSpanExporter[F, Chunk](client, uri), config)
    }
}
