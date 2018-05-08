import sbt.Keys.version

enablePlugins(ScalaJSPlugin)

version := "0.1"

scalaVersion := "2.12.5"

lazy val akkaVersion = "2.5.3"

// This is an application with a main method
scalaJSUseMainModuleInitializer := true

lazy val server = (project in file("server"))
  .settings(
    name := "json-server-bootstrap",
    version := "0.1",
    scalaVersion := "2.12.6",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % "10.1.1",
      "com.typesafe.akka" %% "akka-stream" % "2.5.11",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    )
  )

lazy val client = (project in file("web-client"))
.enablePlugins(ScalaJSPlugin)
.settings(
  name := "json-client-bootstrap",
  version := "0.1",
  scalaVersion := "2.12.6",
  libraryDependencies++=Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.5"
  )
)
