name := "play"
organization in ThisBuild := "com.testapp"
scalaVersion in ThisBuild := "2.12.8"
version := "0.1"

val akkaVersion = "2.5.18"

lazy val common = RootProject(file("../common"))

lazy val play = Project(id = "play", base = file("."))
  .settings(
    settings,
    libraryDependencies ++= Seq(
      dependencies.akkaActor,
      dependencies.akkaCluster,
      dependencies.akkaClusterTools,
      dependencies.akkaMultiNodeTestKit,
      dependencies.scalaTest
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
    val postgresql = "org.postgresql" % "postgresql" % "9.4-1200-jdbc41"
    val playapi = "com.typesafe.play" %% "play-json" % "2.6.8"
  }