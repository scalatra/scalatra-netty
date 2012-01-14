package org.scalatra
package tests

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

  def is = sequential ^
    "A scalatra app should" ^
      "respond to an index request" ! root ^
      "respond to a pathed request" ! get("/hello") { response.body must_== "world" } ^
      "respond to a sub app index request" ! get("/sub") { response.body must_== "TestSub index" } ^
      "respond to a sub app pathed request" ! get("/sub/other") { response.body must_== "TestSub other" } ^
    end

  def root = get("/") {
    println("response? %s" format (response != null))
    println("body: %s" format response.body)
    response.body must_== "OMG! It works!!!"
  }
}