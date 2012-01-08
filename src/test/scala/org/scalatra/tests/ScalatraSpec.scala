package org.scalatra
package tests

import org.specs2.Specification
import org.scalatra.netty.NettyServer
import org.specs2.specification.{Step, Fragments}
import dispatch._

trait ScalatraSpec extends Specification {

  val server = NettyServer(port = FreePort.randomFreePort(), publicDirectory = PublicDirectory("src/test/webapp"))
  val http = new thread.Http

  def mount[TheApp <: Mountable](mountable: => TheApp) {
    server.mount(mountable)
  }
  def mount[TheApp <: Mountable](path: String, mountable: => TheApp) {
    server.mount(path, mountable)
  }

  private def startScalatra = {
    server.start
  }

  private def stopScalatra = {
    http.shutdown()
    server.stop
  }

  override def map(fs: => Fragments) = Step(startScalatra) ^ super.map(fs) ^ Step(stopScalatra)
}
