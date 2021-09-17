package io.janstenpickle.trace4cats.datadog

import java.util.concurrent.TimeUnit
import cats.Foldable
import cats.syntax.foldable._
import cats.syntax.show._
import io.circe.Encoder
import io.circe.generic.semiauto._
import io.janstenpickle.trace4cats.`export`.SemanticTags
import io.janstenpickle.trace4cats.model.{AttributeValue, Batch, ExternalTraceContext, SpanContext}

// implements https://docs.datadoghq.com/api/v1/tracing/
case class DataDogSpan(
  trace_id: BigInt,
  span_id: BigInt,
  parent_id: Option[BigInt],
  name: String,
  service: String,
  resource: String,
  meta: Map[String, String],
  metrics: Map[String, Double],
  start: Long,
  duration: Long,
  error: Option[Int]
)

object DataDogSpan extends ExternalTraceContext[BigInt] {
  override def traceId: SpanContext => BigInt = spanContext => BigInt(1, spanContext.traceId.value.drop(8))
  override def spanId: SpanContext => BigInt = spanContext => BigInt(1, spanContext.spanId.value)
  def fromBatch[F[_]: Foldable](batch: Batch[F]): List[List[DataDogSpan]] =
    batch.spans.toList
      .groupBy(_.context.traceId)
      .values
      .toList
      .map(_.map { span =>
        // IDs use BigInts so that they can be unsigned
        val trceId = traceId(span.context)
        val spnId = spanId(span.context)
        val parentId = span.context.parent.map(parent => BigInt(1, parent.spanId.value))

        val allAttributes = span.allAttributes ++ SemanticTags
          .kindTags(span.kind) ++ SemanticTags.statusTags("")(span.status)

        val startNanos = TimeUnit.MILLISECONDS.toNanos(span.start.toEpochMilli)

        DataDogSpan(
          trceId,
          spnId,
          parentId,
          span.name,
          span.serviceName,
          allAttributes.get("resource.name").fold(span.serviceName)(_.toString),
          allAttributes.collect {
            case (k, AttributeValue.StringValue(value)) => k -> value.value
            case (k, AttributeValue.BooleanValue(value)) if k != "error" => k -> value.value.toString
            case (k, value: AttributeValue.AttributeList) => k -> value.show
          },
          allAttributes.collect {
            case (k, AttributeValue.DoubleValue(value)) => k -> value.value
            case (k, AttributeValue.LongValue(value)) => k -> value.value.toDouble
          },
          startNanos,
          TimeUnit.MILLISECONDS.toNanos(span.end.toEpochMilli) - startNanos,
          allAttributes.get("error").map {
            case AttributeValue.BooleanValue(v) if v.value => 1
            case _ => 0
          }
        )
      })

  implicit val encoder: Encoder[DataDogSpan] = deriveEncoder[DataDogSpan].mapJson(_.dropNullValues)
}
