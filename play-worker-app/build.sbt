name := "play-worker-app"
organization in ThisBuild := "com.testapp"
scalaVersion in ThisBuild := "2.12.8"
version := "0.1"

val akkaVersion = "2.5.18"

// Projects

lazy val global = project
    .in(file("."))
    .aggregate(
      common,
      play,
      worker
    )

lazy val common = project
    .settings(
      settings,
      libraryDependencies ++= Seq(
        dependencies.akkaActor
      )
    )

lazy val play = project
    .settings(
      settings,
      assemblySettings,
      libraryDependencies ++= Seq(
        dependencies.akkaActor,
        dependencies.akkaRemote,
        dependencies.akkaCluster,
        dependencies.akkaClusterMetrics,
        dependencies.akkaClusterTools,
        dependencies.akkaMultiNodeTestKit,
        dependencies.scalaTest
      )
    )
    .dependsOn(
      common
    )

lazy val worker = project
    .settings(
      settings,
      assemblySettings,
      libraryDependencies ++= Seq(
        dependencies.akkaActor,
        dependencies.squeryl,
        dependencies.postgresql,
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
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases",
      "Postgresql repository" at "https://mvnrepository.com/artifact/org.postgresql/postgresql"
    )
)

lazy val dependencies =
    new {
      val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
      val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaVersion
      val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion
      val akkaClusterMetrics = "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion
      val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
      val akkaMultiNodeTestKit = "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion
      val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test
      val squeryl = "org.squeryl" %% "squeryl" % "0.9.5-7"
      val postgresql = "org.postgresql" % "postgresql" % "9.4-1200-jdbc41"
    }

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _ => MergeStrategy.first
  }
)