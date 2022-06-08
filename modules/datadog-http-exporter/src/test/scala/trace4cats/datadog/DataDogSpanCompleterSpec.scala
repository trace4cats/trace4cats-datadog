package trace4cats.datadog

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.blaze.client.BlazeClientBuilder
import org.scalacheck.Shrink
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import trace4cats.CompleterConfig
import trace4cats.model.{CompletedSpan, TraceProcess}
import trace4cats.test.ArbitraryInstances

import scala.concurrent.duration._

class DataDogSpanCompleterSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks with ArbitraryInstances {
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 3, maxDiscardedFactor = 50.0)

  implicit def noShrink[T]: Shrink[T] = Shrink.shrinkAny

  behavior.of("DataDogSpanCompleter")

  it should "send a span to datadog agent without error" in forAll {
    (process: TraceProcess, span: CompletedSpan.Builder) =>
      assertResult(()) {
        BlazeClientBuilder[IO].resource
          .flatMap(c => DataDogSpanCompleter[IO](c, process, config = CompleterConfig(batchTimeout = 100.millis)))
          .use(_.complete(span))
          .unsafeRunSync()
      }
  }
}
