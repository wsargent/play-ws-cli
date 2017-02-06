import Dependencies._

name := "play-ws-cli"

scalaVersion := "2.12.1"
//crossScalaVersions := Seq("2.12.1", "2.11.8")

//resolvers ++= DefaultOptions.resolvers(snapshot = true)
resolvers in ThisBuild += Resolver.sonatypeRepo("public")

libraryDependencies ++= playWs
libraryDependencies ++= logback
libraryDependencies ++= caffeine
libraryDependencies ++= akkaHttp
//libraryDependencies ++= ammonite.map(_ cross CrossVersion.full)

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
  "-Ywarn-dead-code"
)

//initialCommands in (Test, console) := """ammonite.Main().run()"""

mainClass := Some("Main")

fork in run := true
