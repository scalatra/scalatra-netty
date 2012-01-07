package org.scalatra

import netty.{NettySupport, NettyServer}
import java.io.File

class RootApp extends ScalatraApp with NettySupport {

  get("/hello") { "world" }
  get("/") { "It works!" }

}
//
//abstract class SubApp extends ScalatraApp {
//
//  override val basePath = "/"
//
//  override val pathName = "blah"
//
//  val blahPath = path
//
//  get("/test") { "an app nested in another app"}
//
//
////        mount("bar", new ScalatraApp with NettySupport {
////
////          get("/test") { "an app nested in another nested app"}
////
////
////          override val pathName = "bar"
////
////          override val basePath = blahPath
////        })
//}
//
object AppServer extends App {

  val server = new NettyServer
  server.mount(new ScalatraApp with NettySupport {
    get("/hello") { "world" }
    get("/") { "It works!" }

    server.mount("/blah", new ScalatraApp with NettySupport {
      get("/") {
        "index mounted under /blah"
      }
    })
  })
  server.start()
  println("The servers started on %d" format server.port)
  sys.addShutdownHook(server.stop)
//  def main(args: Array[String]) {
//    val server = new NettyServer
//
//    server mount (new RootApp with NettySupport)
//
//  //  server mount new ScalatraApp with NettySupport {
//  //
//  //    get("/") { "testing has an index action" }
//  //    get("/blah") { "testing has sub actions" }
//  //  }
//
//    server.start()
//    sys.addShutdownHook(server.stop())
//  }
}