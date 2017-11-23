
lazy val commonSettings = Seq(
  organization := "com.coding42",
  name := "DynamoS",
  description := "DynamoS is a Scala to DynamoDB conversion library",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.4"
)


lazy val publishSettings = Seq(

//  releaseCrossBuild := true,
//
//  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/adrijardi/DynamoS")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/adrijardi/DynamoS"),
      "scm:git@github.com:adrijardi/DynamoS.git"
    )
  ),
  developers := List(
    Developer("adrijardi", "Adrian Lopez", "adrijardi@gmail.com", url("http://coding42.com"))
  )
)

libraryDependencies ++= Seq(
  "com.propensive" %% "magnolia" % "0.6.1",
  "com.amazonaws"  %  "aws-java-sdk-dynamodb" % "1.11.106"
)

lazy val root = project.in(file("."))
.settings(commonSettings ++ publishSettings)
