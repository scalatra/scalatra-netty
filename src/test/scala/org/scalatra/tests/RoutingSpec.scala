package org.scalatra.tests

import org.scalatra.AppContext


class TestScalatraApp(base: String = "/")(implicit ctxt: AppContext) extends org.scalatra.netty.ScalatraApp(base) {

  get("/") {
    "OMG! It works!!!"
  }

  get("/hello") {
    "world"
  }
}
class RoutingSpec {

}