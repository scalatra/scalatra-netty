package org.scalatra

import netty.NettyServer

object AppServer extends App {

  val server = new NettyServer
  implicit val context = server.appContext
  server.mount(new netty.ScalatraApp("/") {
    get("/hello") { "world" }
    get("/") { "It works!" }
  })
  server.mount(new netty.ScalatraApp("/", "testing") {

    get("/") { "testing has an index action" }
    get("/blah") { "testing has sub actions" }
  })
  server.start()
  sys.addShutdownHook(server.stop())
}