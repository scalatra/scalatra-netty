package org.scalatra.tests

import org.scalatra.AppContext


class TestScalatraApp(ctxt: AppContext, base: String = "/") extends org.scalatra.netty.ScalatraApp(ctxt, base) {

  get("/") {
    "OMG! It works!!!"
  }

  get("/hello") {
    "world"
  }
}
class RoutingSpec {

}