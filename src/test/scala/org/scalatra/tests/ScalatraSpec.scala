package org.scalatra
package tests

import org.specs2.Specification
import org.scalatra.netty.NettyServer
import org.specs2.specification.{Step, Fragments}


trait ScalatraSpec extends Specification {

  val nettyServer = new NettyServer

  override def map(fs: => Fragments) = Step(nettyServer.start) ^ super.map(fs) ^ Step(nettyServer.stop)
}
