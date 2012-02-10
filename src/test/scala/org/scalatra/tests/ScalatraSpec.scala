package org.scalatra
package tests

import org.specs2.Specification
import org.scalatra.netty.NettyServer
import org.specs2.specification.{Step, Fragments}
import scala.util.control.Exception._
import java.io.{File, FileNotFoundException}
import java.net.{URI, URISyntaxException}

trait ScalatraSpec extends Specification with Client {

  val server = NettyServer(FreePort.randomFreePort(), PublicDirectory("src/test/webapp"))
  val serverClient: Client = new NettyClient("127.0.0.1", server.port)
  
  protected implicit def fileToSeq(file: File): Seq[File] = Seq(file)
    

  def mount[TheApp <: Mountable](mountable: => TheApp) {
    server.mount(mountable)
  }
  def mount[TheApp <: Mountable](path: String, mountable: => TheApp) {
    server.mount(path, mountable)
  }

  private def startScalatra = {
    server.start
    serverClient.start()
  }

  private def stopScalatra = {
    ignoring(classOf[Throwable]) { serverClient.stop() }
    ignoring(classOf[Throwable]) { server.stop }
  }

  override def map(fs: => Fragments) = Step(startScalatra) ^ super.map(fs) ^ Step(stopScalatra)

  def submit[A](method: String, uri: String, params: Iterable[(String, String)], headers: Map[String, String], files: Seq[File], body: String)(f: => A) =
    serverClient.submit(method, uri, params, headers, files, body){
      withResponse(serverClient.response)(f)
    }


  override def session[A](f: => A) = {
    serverClient._cookies.withValue(Nil) {
      serverClient._useSession.withValue(true) {
        _cookies.withValue(serverClient.cookies) {
          _useSession.withValue(serverClient.useSession)(f)
        }
      }
    }
  }

  def classpathFile(path: String) = {
    val cl = allCatch.opt(Thread.currentThread.getContextClassLoader) getOrElse getClass.getClassLoader
    try{
      new File(new URI(cl.getResource(path).toString).getSchemeSpecificPart)
    } catch {
      case e: URISyntaxException => throw new FileNotFoundException(path)
    }
  }
}
