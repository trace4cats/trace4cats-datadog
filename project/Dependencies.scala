import sbt._

object Dependencies {
  object Versions {
    val scala212 = "2.12.16"
    val scala213 = "2.13.8"
    val scala3 = "3.3.0"

    val trace4cats = "0.14.5"
    val trace4catsExporterHttp = "0.14.0"

    val circe = "0.14.8"
    val http4s = "0.23.14"
    val http4sBlaze = "0.23.12"

    val kindProjector = "0.13.2"
    val betterMonadicFor = "0.3.1"
  }

  lazy val trace4catsCore = "io.janstenpickle"         %% "trace4cats-core"          % Versions.trace4cats
  lazy val trace4catsTestkit = "io.janstenpickle"      %% "trace4cats-testkit"       % Versions.trace4cats
  lazy val trace4catsExporterHttp = "io.janstenpickle" %% "trace4cats-exporter-http" % Versions.trace4catsExporterHttp

  lazy val circeGeneric = "io.circe"        %% "circe-generic"       % Versions.circe
  lazy val http4sCirce = "org.http4s"       %% "http4s-circe"        % Versions.http4s
  lazy val http4sBlazeClient = "org.http4s" %% "http4s-blaze-client" % Versions.http4sBlaze

  lazy val kindProjector = ("org.typelevel" % "kind-projector"     % Versions.kindProjector).cross(CrossVersion.full)
  lazy val betterMonadicFor = "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor
}
