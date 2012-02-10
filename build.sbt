import akka.sbt._

organization := "org.scalatra"

organizationHomepage := Some(url("http://scalatra.org"))

organizationName := "Scalatra"

name := "scalatra-netty"

resolvers ++= Seq(
  "ScalaTools Snapshots nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots",
  "repository.jboss.org" at "https://repository.jboss.org/nexus/content/repositories/releases/",
  "Akka Repository" at "http://akka.io/repository"
)

libraryDependencies ++= Seq(
  "com.mojolly.rl"                   %% "rl"                % "0.2.5-SNAPSHOT",
  "eu.medsea.mimeutil"                % "mime-util"         % "2.1.3",
  "com.googlecode.juniversalchardet"  % "juniversalchardet" % "1.0.3",
  "com.ning"                          % "async-http-client" % "1.7.0",
  "com.github.scala-incubator.io"    %% "scala-io-core"     % "0.3.0",
  "com.github.scala-incubator.io"    %% "scala-io-file"     % "0.3.0",
  "com.typesafe.akka"                 % "akka-actor"        % "2.0-M4",
  "org.parboiled"                     % "parboiled-scala"   % "1.0.2",
  "io.netty"                          % "netty"             % "3.3.1.Final",
  "org.scalaz"                       %% "scalaz-core"       % "6.0.4",
  "com.google.guava"                  % "guava"             % "10.0.1",
  "org.specs2"                       %% "specs2"            % "1.7.1"             % "test",
  "org.scala-tools.testing"          %% "scalacheck"        % "1.9"               % "test"
)

ivyXML := <dependencies>
    <exclude module="log4j" />
    <exclude module="slf4j-log4j12" />
    <exclude module="slf4j-api-1.6.0"  />
    <exclude module="org.jboss.netty" />
  </dependencies>

seq(VersionGenPlugin.allSettings :_*)

seq(AkkaKernelPlugin.distSettings :_*)

testOptions in Test += Tests.Setup( () => System.setProperty("akka.mode", "test") )

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "console", "junitxml")

testOptions in Test <+= (crossTarget map { ct =>
 Tests.Setup { () => System.setProperty("specs2.junit.outDir", new File(ct, "specs-reports").getAbsolutePath) }
})

