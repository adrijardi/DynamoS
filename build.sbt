organization := "com.coding42"

name := "DynamoS"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.propensive" %% "magnolia" % "0.6.1",
  "com.amazonaws"  %  "aws-java-sdk-dynamodb" % "1.11.106"
)
