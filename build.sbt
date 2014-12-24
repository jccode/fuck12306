
val json4sNative = "org.json4s" %% "json4s-native" % "3.2.10"
val commonsMail = "org.apache.commons" % "commons-email" % "1.2"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
val log4j = "org.slf4j" % "slf4j-log4j12" % "1.7.9"


lazy val root = (project in file(".")).
  settings(
    name := "Fuck12306",
    version := "1.0",
    scalaVersion := "2.11.4",
    libraryDependencies ++= Seq(
      json4sNative,
      commonsMail,
      scalaLogging,
      log4j
    )
  )

