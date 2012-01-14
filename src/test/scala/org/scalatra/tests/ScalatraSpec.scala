package org.scalatra
package tests

import org.specs2.Specification
import org.scalatra.netty.NettyServer
import org.specs2.specification.{Step, Fragments}

trait ScalatraSpec extends Specification with Client {

  val server = NettyServer(port = FreePort.randomFreePort(), publicDirectory = PublicDirectory("src/test/webapp"))
  val serverClient: Client = new NettyClient("127.0.0.1", server.port)

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
    serverClient.stop()
    server.stop
  }

  override def map(fs: => Fragments) = Step(startScalatra) ^ super.map(fs) ^ Step(stopScalatra)

  def submit[A](method: String, uri: String, params: Iterable[(String, String)], headers: Map[String, String], body: String)(f: => A) =
    serverClient.submit(method, uri, params, headers, body){
      withResponse(serverClient.response)(f)
    }
}
