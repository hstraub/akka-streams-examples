name := "akka-streams-examples"
 
version := "0.1.0 "
 
scalaVersion := "2.11.7"

val akkaVersion = "2.3.12"

val akkaStreamVersion = "1.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-agent" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaStreamVersion
)

scalacOptions ++= Seq("-feature")
