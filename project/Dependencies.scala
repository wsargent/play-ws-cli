import sbt._

object Dependencies {

  val specsVersion = "3.8.6"
  val specsBuild = Seq(
    "specs2-core",
    "specs2-junit",
    "specs2-mock"
  ).map("org.specs2" %% _ % specsVersion)

  val akkaHttpVersion = "10.0.3"
  val akkaHttp = Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  )

  val ammoniteVersion = "0.8.2"
  val ammonite = Seq("com.lihaoyi" % "ammonite" % ammoniteVersion)

  // https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/jcache
  val caffeine = Seq(
    "com.google.code.findbugs" % "jsr305" % "3.0.1" % Compile,
    "com.github.ben-manes.caffeine" % "jcache" % "2.3.5"
  )

  val playWsVersion = "1.0.0-M3"
  val playWs = Seq("com.typesafe.play" %% "play-ahc-ws-standalone" % playWsVersion)

  val logbackVersion =  "1.1.9"
  val logback = Seq("ch.qos.logback" % "logback-classic" % logbackVersion)

}
