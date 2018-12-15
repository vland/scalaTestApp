import sbt.Keys._

name := "worker"
scalaVersion := "2.12.8"
version := "0.1"

val akkaVersion = "2.5.19"

lazy val common = RootProject(file("../common"))

lazy val worker = Project(id = "worker", base = file("."))

  .settings(
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      dependencies.akkaActor,
      dependencies.akkaCluster,
      dependencies.akkaClusterTools,
      dependencies.squeryl,
      dependencies.mysqlDriver,
      dependencies.scalaTest,
      guice,
      evolutions,
      jdbc
    )
  )
  .dependsOn(
    common
  )

lazy val settings = Seq(
  scalacOptions ++= Seq(
    "-unchecked",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-deprecation",
    "-encoding",
    "utf8"
  ),
  resolvers ++= Seq(
    "Default repository" at "https://repo1.maven.org/maven2/"
  )
)

lazy val dependencies =
  new {
    val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
    val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion
    val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
    val akkaMultiNodeTestKit = "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
    val squeryl = "org.squeryl" %% "squeryl" % "0.9.5-7"
    val mysqlDriver = "mysql" % "mysql-connector-java" % "5.1.10"
    val playapi = "com.typesafe.play" %% "play-json" % "2.6.8"
  }

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
  }
)
