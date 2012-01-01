package org.scalatra

import netty.NettyServer

object AppServer extends App {

  val server = new NettyServer
  implicit val context = server.appContext
  server.mount(new netty.ScalatraApp("/") {
    get("/hello") { "world" }
    get("/") { "It works!" }
  })
  server.mount(new netty.ScalatraApp("/") {

    get("/") { "testing has an index action" }
    get("/blah") { "testing has sub actions" }
    pathName = "testing"
  })
  server.start()
  sys.addShutdownHook(server.stop())
}