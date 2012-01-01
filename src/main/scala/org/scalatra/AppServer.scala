package org.scalatra

import netty.NettyServer

object AppServer extends App {

  val server = new NettyServer
  server.start()
  sys.addShutdownHook(server.stop())
}