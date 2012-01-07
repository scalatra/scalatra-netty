package org.scalatra

import netty.{NettySupport, NettyServer}

object AppServer extends App {

  val server = new NettyServer
  server.mount(new ScalatraApp with NettySupport {
    get("/hello") { "world" }
    get("/") { "It works!" }

    mount("blah", new ScalatraApp with NettySupport {
      get("/") {
        "index mounted under /blah"
      }
    })
  })
  server.start()
  println("The servers started on %d" format server.port)
  sys.addShutdownHook(server.stop)
}