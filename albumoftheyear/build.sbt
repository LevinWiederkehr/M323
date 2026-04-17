ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "3.3.1"

name := "albumvault"

libraryDependencies ++= Seq(
  // HTTP-Client für MusicBrainz API
  "com.softwaremill.sttp.client3" %% "core" % "3.9.0",
  // JSON verarbeiten
  "com.lihaoyi" %% "ujson" % "3.1.3",
  // Für Tests (optional aber empfohlen)
  "org.scalatest" %% "scalatest" % "3.2.17" % Test
)

Test / parallelExecution := true