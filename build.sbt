name := "SearchEngine"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2"
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.2.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)
    