name := """band-merchandise"""
organization := "com.ryan"
version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.3.4"

libraryDependencies ++= Seq(
    guice,
    evolutions,
    jdbc,
    "org.playframework.anorm" %% "anorm" % "2.7.0",
    "org.postgresql" % "postgresql" % "42.7.3",
    "com.konghq" % "unirest-java" % "3.14.5"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
