package org.scalatra

import netty.{NettyServer}

object AppServer extends App {

  // mounts the app in the block as a root application
  val server = NettyServer( PublicDirectory("src/main/webapp")) {
    new ScalatraApp {
      get("/hello") { "world" }
      get("/") { "It works!" }

      mount("blah", new ScalatraApp  {
        get("/") {
          "index mounted under /blah"
        }
      })
    }
  }
  server.start()
  println("The servers started on %d" format server.port)
}