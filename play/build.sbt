import sbt.Keys._

version := "0.1"
scalaVersion := "2.12.8"

val akkaVersion = "2.5.19"

lazy val common = RootProject(file("../common"))

lazy val playapp = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """play-app""",
    libraryDependencies ++= Seq(
      dependencies.akkaActor,
      dependencies.akkaCluster,
      dependencies.akkaClusterTools,
      dependencies.scalaTest,
      dependencies.logger,
      guice
    )
  )
  .dependsOn(
    common
)

lazy val dependencies =
  new {
    val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
    val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion
    val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
    val squeryl = "org.squeryl" %% "squeryl" % "0.9.5-7"
    val postgresql = "org.postgresql" % "postgresql" % "9.4-1200-jdbc41"
    val playapi = "com.typesafe.play" %% "play-json" % "2.6.8"
    val logger = "org.slf4j" % "slf4j-simple" % "1.6.4"
  }