import akka.sbt._

resolvers += "ScalaTools Snapshots nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots"

resolvers += "repository.jboss.org" at "https://repository.jboss.org/nexus/content/repositories/releases/"

resolvers += "Akka Repository" at "http://akka.io/repository"

parallelExecution in test := false

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.6.4",
  "org.slf4j" % "log4j-over-slf4j" % "1.6.4",
  "com.mojolly.rl" %% "rl" % "0.2.5-SNAPSHOT",
  "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.7",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "provided",
  "eu.medsea.mimeutil" % "mime-util" % "2.1.3",
  "net.iharder" % "base64" % "2.3.8",
  "com.ning" % "async-http-client" % "1.7.0",
  "org.scala-tools.time" %% "time" % "0.5",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.3.0",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.3.0",
  "com.typesafe.akka" % "akka-kernel" % "2.0-M2" % "provided;runtime",
  "org.jboss.netty" % "netty" % "3.2.7.Final",
  "org.scalaz" %% "scalaz-core" % "6.0.3",
  "com.google.guava" % "guava" % "10.0.1",
  "org.specs2" %% "specs2" % "1.7.1" % "test"
)

ivyXML := <dependencies>
    <exclude module="log4j" />
    <exclude module="slf4j-log4j12" />
    <exclude module="slf4j-api-1.6.0"  />
  </dependencies>

seq(AkkaKernelPlugin.distSettings :_*)