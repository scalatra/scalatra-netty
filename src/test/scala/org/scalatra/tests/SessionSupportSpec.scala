package org.scalatra
package tests

class SessionSupportTestApp extends ScalatraApp with SessionSupport {
  
  get("/") {
    session("my-key") = "the session value"
  }
  
  get("/my-key") {
    session("my-key")
  }
}

class SessionSupportSpec extends ScalatraSpec {

  def is =
    "SessionSupport should" ! pending ^ end

}
