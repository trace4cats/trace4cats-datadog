ThisBuild / scalaVersion := Dependencies.Versions.scala213
ThisBuild / crossScalaVersions := Seq(Dependencies.Versions.scala213, Dependencies.Versions.scala212)
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowJavaVersions := Seq("adopt@1.8", "adopt@1.11")

ThisBuild / githubWorkflowBuildPreamble += WorkflowStep.Run(
  commands = List("docker-compose up -d"),
  name = Some("Create and start Docker containers")
)
ThisBuild / githubWorkflowBuildPreamble += WorkflowStep.Sbt(
  List("scalafmtCheckAll", "scalafmtSbtCheck"),
  name = Some("Check formatting")
)
ThisBuild / githubWorkflowBuildPostamble += WorkflowStep.Run(
  commands = List("docker-compose down"),
  name = Some("Stop and remove Docker resources")
)

ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.Equals(Ref.Branch("master")))
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ciReleaseSonatype"),
    name = Some("Publish artifacts"),
    env = Map(
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)
ThisBuild / githubWorkflowPublishCond := Some("github.actor != 'mergify[bot]'")
ThisBuild / githubWorkflowPublishPreamble += WorkflowStep.Use(
  ref = UseRef.Public("crazy-max", "ghaction-import-gpg", "v3"),
  id = Some("import_gpg"),
  name = Some("Import GPG key"),
  params = Map("gpg-private-key" -> "${{ secrets.GPG_PRIVATE_KEY }}", "passphrase" -> "${{ secrets.PGP_PASS }}")
)

ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / versionScheme := Some("early-semver")

ThisBuild / licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
ThisBuild / developers := List(
  Developer(
    "janstenpickle",
    "Chris Jansen",
    "janstenpickle@users.noreply.github.com",
    url = url("https://github.com/janstepickle")
  ),
  Developer(
    "catostrophe",
    "λoλcat",
    "catostrophe@users.noreply.github.com",
    url = url("https://github.com/catostrophe")
  )
)
ThisBuild / homepage := Some(url("https://github.com/trace4cats"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/trace4cats/trace4cats-datadog"),
    "scm:git:git@github.com:trace4cats/trace4cats-datadog.git"
  )
)
ThisBuild / organization := "io.janstenpickle"
ThisBuild / organizationName := "trace4cats"
