package org.scalatra
package tests

class CookiesTestApp extends ScalatraApp {
  get("/") {
    //request.cookies += "the-cookie" -> "cookies are sweet"
    "hello"
  }
}

class CookieSupportSpec extends ScalatraSpec {

  mount(new CookiesTestApp)

  def is =
    "CookieSupport should" ^ end
}

