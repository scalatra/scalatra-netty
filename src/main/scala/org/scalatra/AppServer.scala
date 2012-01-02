package org.scalatra

import netty.{NettySupport, NettyServer}


object AppServer extends App {

  val server = new NettyServer
  implicit val context = server.appContext
  server mount (new ScalatraApp with NettySupport {

    val rootPath = path
    get("/hello") { "world" }
    get("/") { "It works!" }

    this mount (new ScalatraApp with NettySupport {
      override val basePath = rootPath
      override val pathName = "blah"

      val blahPath = path
      get("/test") { "an app nested in another app"}
      
      this mount (new ScalatraApp with NettySupport {
        override val pathName = blahPath
        override val basePath = "bar"

        get("/test") { "an app nested in another nested app"}
      })
    })
  })
    
  server mount new ScalatraApp with NettySupport {

    get("/") { "testing has an index action" }
    get("/blah") { "testing has sub actions" }
  }
  
  server.start()
  sys.addShutdownHook(server.stop())
}