package org.scalatra

import netty.{NettySupport, NettyServer}


object AppServer  {

  val server = new NettyServer
  
  server mount (new ScalatraApp with NettySupport {
    get("/hello") { "world" }
    get("/") { "It works!" }
    mount("blah", new ScalatraApp with NettySupport {
      val blahPath = path
      get("/test") { "an app nested in another app"}
      mount("bar", new ScalatraApp with NettySupport {
        override val pathName = blahPath
        get("/test") { "an app nested in another nested app"}
      })
    })
  })
    
//  server mount new ScalatraApp with NettySupport {
//
//    get("/") { "testing has an index action" }
//    get("/blah") { "testing has sub actions" }
//  }
  
  server.start()
  sys.addShutdownHook(server.stop())
}