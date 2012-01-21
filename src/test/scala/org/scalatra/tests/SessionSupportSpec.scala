package org.scalatra
package tests

class SessionSupportTestApp extends ScalatraApp with SessionSupport {
  
  get("/set") {
    session("my-key") = "the session value"
    "OK"
  }
  
  get("/get") {
    session.get("my-key") getOrElse "failed"
  }
  
  error {
    case e => {
      e.printStackTrace()
      "failed"
    }
  }
}

class SessionSupportSpec extends ScalatraSpec {

  mount(new SessionSupportTestApp)

  def is =
    "SessionSupport should set and get a value" ! {
      session {
        (get("/set") { status.code must_== 200 }) and 
        (get("/get") { body must_== "the session value"} )
      }
    } ^ end

}
