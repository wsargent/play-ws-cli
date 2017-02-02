name := "ws-cache-example"

version := "1.0.0"

scalaVersion := "2.12.1"

resolvers ++= DefaultOptions.resolvers(snapshot = true)
resolvers in ThisBuild += Resolver.sonatypeRepo("public")

val specsVersion = "3.8.6"
val specsBuild = Seq(
  "specs2-core",
  "specs2-junit",
  "specs2-mock"
).map("org.specs2" %% _ % specsVersion)

// https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/jcache
val caffeine = Seq(
  "com.google.code.findbugs" % "jsr305" % "3.0.1" % Compile,
  "com.github.ben-manes.caffeine" % "jcache" % "2.3.5"
)

libraryDependencies += ("com.typesafe.play" %% "play-ahc-ws-standalone" % "1.0.0-SNAPSHOT")
libraryDependencies += ("com.typesafe.play" %% "shaded-asynchttpclient" % "1.0.0-SNAPSHOT")
libraryDependencies += ("com.typesafe.play" %% "shaded-oauth" % "1.0.0-SNAPSHOT")

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.9"

libraryDependencies ++= caffeine

libraryDependencies ++= specsBuild.map(_ % Test)

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.2" % Test

mainClass := Some("Main")

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

fork in run := true
