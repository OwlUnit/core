import sbt._
import Keys._

object IiCoreBuild extends Build {

  lazy val root = Project(
    id = "core",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      resolvers ++= Seq("Neo4j Maven 2 release repository" at "http://m2.neo4j.org/releases"),
      libraryDependencies ++= Seq(
        Dependency.specs2,
        Dependency.neo4j,
        Dependency.neo4jREST,
        Dependency.akka,
        Dependency.slf4s,
        Dependency.logback
        )
      )
    )

  /////////////////////
  // Settings
  /////////////////////

  override lazy val settings = super.settings ++ Seq(
    organization := "com.owlunit",
    version := "0.3-SNAPSHOT",
    scalaVersion := "2.9.1",
    resolvers += typesafe,
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
    javacOptions ++= Seq("-Xlint:unchecked"),
    publishTo := Some(Resolver.file("file",  new File( "../../owlunit.github.com/repo/ivy/" )))
  )

  val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

  /////////////////////
  // Dependencies
  /////////////////////

  object Dependency {

    val neo4j       = "org.neo4j"                 %  "neo4j"                % "1.8"
    val neo4jREST   = "org.neo4j"                 %  "neo4j-rest-graphdb"   % "1.8"

    val akka        = "com.typesafe.akka"         % "akka-actor"            % "2.0.4"

    val slf4s       = "com.weiglewilczek.slf4s"   %% "slf4s"                % "1.0.7"
    val specs2      = "org.specs2"                %% "specs2"               % "1.9"    % "test"
    val logback     = "ch.qos.logback"            %  "logback-classic"      % "1.0.6"  % "test"

  }

}
