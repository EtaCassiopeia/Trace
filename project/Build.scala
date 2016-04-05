import sbt._
import Keys._

object MyBuild extends Build {

  lazy val commonSettings = Seq(
    organization := "com.github.etacassiopeia",
    version := "0.1.0",
    scalaVersion := "2.11.8",
    scalacOptions in Compile ++= Seq("-unchecked", "-feature", "-language:postfixOps", "-deprecation", "-encoding", "UTF-8")
  )

  lazy val root = project.in(file(".")).aggregate(common, client, stream, query, web)

  lazy val common = project.settings(commonSettings: _*)
  lazy val stream = project.settings(commonSettings: _*)
  lazy val client = project.dependsOn(common).settings(commonSettings: _*)
    .settings(
      libraryDependencies := Seq(
        "org.apache.kafka" % "kafka-clients" % "0.9.0.1",
        "com.typesafe.akka" % "akka-actor_2.11" % "2.4.3",
        "com.typesafe" % "config" % "1.3.0",
        "com.twitter" % "chill_2.11" % "0.8.0"
      )
    )
  lazy val query = project.settings(commonSettings: _*)
  lazy val web = project.settings(commonSettings: _*)
}