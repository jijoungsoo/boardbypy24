name := """boardbypy24"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
	cache
  ,ws
  ,specs2 % Test
)

libraryDependencies ++= Seq(
   "com.typesafe.slick" %% "slick" % "3.0.0"
   ,"com.typesafe.play" %% "play-slick" % "1.0.0"
	,"com.typesafe.play" %% "play-slick-evolutions" % "1.0.0"
	,"mysql" % "mysql-connector-java" % "5.1.35"
)

libraryDependencies += evolutions
libraryDependencies += "org.lazyluke" % "log4jdbc-remix" % "0.2.7"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


fork in run := true