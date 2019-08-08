
scalaVersion := "2.12.8"

name := "pollster-service"
organization := "com.newlibertie.pollster"
version := "0.5"

lazy val akkaHttpVersion = "10.1.5"
lazy val akkaVersion = "2.5.17"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.1" % Test
)


libraryDependencies += "org.typelevel" %% "cats-core" % "1.6.0"
// https://mvnrepository.com/artifact/net.liftweb/lift-json
libraryDependencies += "net.liftweb" %% "lift-json" % "3.1.1"
// https://mvnrepository.com/artifact/mysql/mysql-connector-java
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.16"

// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.9.9"
// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
//libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.0.0-RC3"

// https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-scala
//libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala" % "2.0.2"
//libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala" % "2.4.4"


libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.9"


// https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.5.6" % Test


libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.11.1"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.11.1"