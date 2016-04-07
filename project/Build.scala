import sbt._
import Keys._


object TraceBuild extends Build {

  lazy val copyDependencies = TaskKey[Unit]("copy-dependencies")

  def copyDepTask = copyDependencies <<= (update, crossTarget, scalaVersion) map {
    (updateReport, out, scalaVer) =>
      updateReport.select(configuration = Set("compile")) foreach { srcPath =>
        val destPath = out / "lib" / srcPath.getName
        IO.copyFile(srcPath, destPath, preserveLastModified = true)
      }
  }

  lazy val commonSettings = Seq(
    organization := "com.github.etacassiopeia",
    version := "0.1.0",
    scalaVersion := "2.11.8",
    scalacOptions in Compile ++= Seq("-unchecked", "-feature", "-language:postfixOps", "-deprecation", "-encoding", "UTF-8"),
    scalacOptions += "-Ylog-classpath",
    evictionWarningOptions in evicted := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    ivyScala := ivyScala.value map {
      _.copy(overrideScalaVersion = true)
    }
    //retrieveManaged := true
  )

  lazy val root = project.in(file(".")).aggregate(common, client, stream, query, ui)

  lazy val common = project.settings(commonSettings: _*)
  lazy val stream = project.settings(commonSettings: _*).settings(
    libraryDependencies := Seq(
      "org.apache.spark" % "spark-core_2.11" % "1.6.1",
      "org.apache.spark" % "spark-streaming_2.11" % "1.6.1",
      "org.apache.spark" % "spark-streaming-kafka_2.11" % "1.6.1" % "compile",
      "joda-time" % "joda-time" % "2.9.3",
      "org.eclipse.jetty" % "jetty-servlet" % "9.3.8.v20160314"
    ),
    copyDepTask
  )
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
  lazy val ui = project.settings(commonSettings: _*)
}