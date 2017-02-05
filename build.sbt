import Dependencies._

name := "ws-cache-example"

scalaVersion := "2.12.1"

//resolvers ++= DefaultOptions.resolvers(snapshot = true)
resolvers in ThisBuild += Resolver.sonatypeRepo("public")

libraryDependencies ++= playWs
libraryDependencies ++= logback
libraryDependencies ++= caffeine
libraryDependencies ++= akkaHttp

// XXX FIXME Should not need explicit dependencies, this should come in from playWS!
libraryDependencies ++= shaded

libraryDependencies ++= specsBuild.map(_ % Test)

scalacOptions in (Compile, doc) ++= Seq(
  "-target:jvm-1.8",
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Ywarn-unused-import",
  "-Ywarn-nullary-unit",
  "-Xfatal-warnings",
  "-Xlint",
  "-Ydebug", // debug compiler
  "-Ywarn-dead-code"
)

mainClass := Some("Main")

fork in run := true
