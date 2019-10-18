lazy val commonSettings = Seq(
  organization := "com.coding42",
  name := "DynamoS",
  description := "DynamoS is a Scala to DynamoDB conversion library",
  version := "0.4.0",
  scalaVersion := "2.12.9",
  crossScalaVersions := Seq("2.11.12", "2.12.9", "2.13.1"),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
    "-Ypartial-unification"
  )
)

lazy val testSettings = Seq(
  startDynamoDBLocal := startDynamoDBLocal.dependsOn(compile in Test).value,
  test in Test := (test in Test).dependsOn(startDynamoDBLocal).value,
  testOnly in Test := (testOnly in Test)
    .dependsOn(startDynamoDBLocal)
    .evaluated,
  testOptions in Test += dynamoDBLocalTestCleanup.value
)

lazy val publishSettings = Seq(
  useGpgAgent := true,
  homepage := Some(url("https://github.com/adrijardi/DynamoS")),
  licenses := Seq(
    "Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
  ),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/adrijardi/DynamoS"),
      "scm:git@github.com:adrijardi/DynamoS.git"
    )
  ),
  developers := List(
    Developer(
      "adrijardi",
      "Adrian Lopez",
      "adrijardi@gmail.com",
      url("http://coding42.com")
    )
  )
)

libraryDependencies ++= Seq(
  "com.propensive" %% "magnolia" % "0.6.1",
  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.106",
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "com.lightbend.akka" %% "akka-stream-alpakka-dynamodb" % "0.14" % Test,
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % Test
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings ++ testSettings ++ publishSettings)
