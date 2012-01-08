package org.scalatra
package tests

import dispatch._

class TestSupApp extends ScalatraApp {
  get("/") {
    "TestSub index"
  }
  
  get("/other") {
    "TestSub other"
  }
}

class TestScalatraApp extends ScalatraApp  {

  get("/") {
    "OMG! It works!!!"
  }

  get("/hello") {
    "world"
  }
  
  mount("/sub", new TestSupApp)
}
class RoutingSpec extends ScalatraSpec {

  mount(new TestScalatraApp)

  def is =
    "A scalatra app should" ^
      "respond to an index request" ! testHttpRequest("/", "OMG! It works!!!") ^
      "respond to a pathed request" ! testHttpRequest("/hello", "world") ^
      "respond to a sub app index request" ! testHttpRequest("/sub", "TestSub index") ^
      "respond to a sub app pathed request" ! testHttpRequest("/sub/other", "TestSub other") ^
    end

  def testHttpRequest(path: String, expected: String) = {
    val pth = if (path.startsWith("/")) path.substring(1) else path
    val ptth = if (pth.endsWith("/")) pth.substring(0, pth.length - 1) else pth
    println("Making request to http://localhost:%s/%s" format (server.port, ptth))
    val res = http(:/("localhost", server.port) / ptth as_str)()
    res must_== expected
  }
}