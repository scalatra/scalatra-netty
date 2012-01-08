package org.scalatra

import netty.{NettyServer}

object AppServer extends App {

  val server = NettyServer(publicDirectory = PublicDirectory("src/main/webapp"))
  server.mount(new ScalatraApp {
    get("/hello") { "world" }
    get("/") { "It works!" }

    mount("blah", new ScalatraApp  {
      get("/") {
        "index mounted under /blah"
      }
    })
  })
  server.start()
  println("The servers started on %d" format server.port)
  sys.addShutdownHook(server.stop)
}