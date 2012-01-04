package org.scalatra
package tests

import netty.NettySupport


class TestScalatraApp extends ScalatraApp with NettySupport {

  get("/") {
    "OMG! It works!!!"
  }

  get("/hello") {
    "world"
  }
}
class RoutingSpec {

}