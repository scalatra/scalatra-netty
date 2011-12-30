import akka.sbt._

resolvers += "ScalaTools Snapshots nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots"

resolvers += "repository.jboss.org" at "https://repository.jboss.org/nexus/content/repositories/releases/"

resolvers += "Akka Repository" at "http://akka.io/repository"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor" % "2.0-M1" % "provided",
  "org.jboss.netty" % "netty" % "3.2.7.Final",
  "org.scalaz" %% "scalaz-core" % "6.0.3",
  "com.google.guava" % "guava" % "10.0.1"
)

seq(AkkaKernelPlugin.distSettings :_*)