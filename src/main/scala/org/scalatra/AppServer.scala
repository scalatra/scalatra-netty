package org.scalatra

import netty.NettyServer

object AppServer extends App {

  val server = new NettyServer
  implicit val context = server.appContext
  server mount new netty.ScalatraApp() {
    
    get("/hello") { "world" }
    get("/") { "It works!" }

    this mount new netty.ScalatraApp(basePath = path, pathName = "blah"){
      
      get("/test") { "an app nested in another app"}
      
      this mount new netty.ScalatraApp(basePath = path, pathName = "bar"){
        
        get("/test") { "an app nested in another nested app"}
      }
    }
  }
    
  server mount new netty.ScalatraApp(pathName = "testing") {

    get("/") { "testing has an index action" }
    get("/blah") { "testing has sub actions" }
  }
  
  server.start()
  sys.addShutdownHook(server.stop())
}