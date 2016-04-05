name := "Trace"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions in Compile ++= Seq("-unchecked", "-feature", "-language:postfixOps", "-deprecation", "-encoding", "UTF-8")

libraryDependencies := Seq(
  "org.apache.kafka"      % "kafka-clients"           % "0.9.0.1",
  "com.typesafe.akka"     % "akka-actor_2.11"         % "2.4.3",
  "com.typesafe"          % "config"                  % "1.3.0",
  "com.twitter"           % "chill_2.11"              % "0.8.0"
)