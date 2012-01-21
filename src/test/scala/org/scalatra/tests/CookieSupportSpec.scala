package org.scalatra
package tests

class CookiesTestApp extends ScalatraApp {
  get("/getcookie") {
    request.cookies.get("anothercookie") foreach { cookie =>
      response.headers += "X-Another-Cookie" -> cookie
    }
    request.cookies.get("somecookie") match {
      case Some(v) => v
      case _ => "None"
    }
  }

  post("/setcookie") {
    request.cookies.update("somecookie", params("cookieval"))
    params.get("anothercookieval") foreach { request.cookies("anothercookie") = _ }
    "OK"
  }

  post("/setexpiringcookie") {
    request.cookies.update("thecookie", params("cookieval"))(CookieOptions(maxAge = params("maxAge").toInt))
  }

  post("/set-http-only-cookie") {
    request.cookies.update("thecookie", params("cookieval"))(CookieOptions(httpOnly = true))
  }

  post("/maplikeset") {
    request.cookies += ("somecookie" -> params("cookieval"))
    "OK"
  }

  post("/remove-cookie") {
    request.cookies -= "somecookie"
    response.headers += "Somecookie-Is-Defined" -> request.cookies.get("somecookie").isDefined.toString
  }

  post("/remove-cookie-with-path") {
    request.cookies.delete("somecookie")(CookieOptions(path = "/bar"))
  }
}

class CookieSupportSpec extends ScalatraSpec {

  mount("/foo", new CookiesTestApp)

  def is =
    "CookieSupport should" ^
      "GET /getcookie with no cookies set should return 'None'" ! noCookies ^
      "POST /setcookie with a value should return OK" ! setsCookie ^
      "GET /getcookie with a cookie should set return the cookie value" ! returnsSetCookie ^
      end

  def noCookies = {
    get("/foo/getcookie") {
      response.body must_== "None"
    }
  }

  def setsCookie = {
    post("/foo/setcookie", "cookieval" -> "The value") {
      response.headers("Set-Cookie") must startWith("""somecookie=The value;""") and
      (response.cookies("somecookie").value must_== "The value")
    }
  }

  def returnsSetCookie = {
    session {
      post("/foo/setcookie", "cookieval" -> "The value") {
        body must_== "OK"
      }
      get("/foo/getcookie") {
        body must_== "The value"
      }
    }
  }

  /*

    // Jetty apparently translates Max-Age into Expires?
    ignore("POST /setexpiringcookie should set the max age of the cookie") {
      post("/foo/setexpiringcookie", "cookieval" -> "The value", "maxAge" -> oneWeek.toString) {
        response.getHeader("Set-Cookie") must equal("""thecookie="The value"; Max-Age=604800""")
      }
    }

    test("cookie path defaults to context path") {
      post("/foo/setcookie", "cookieval" -> "whatever") {
        response.getHeader("Set-Cookie") must include (";Path=/foo")
      }
    }

    test("cookie path defaults to context path when using a maplike setter") {
      post("/foo/maplikeset", "cookieval" -> "whatever") {
        val hdr = response.getHeader("Set-Cookie")
        hdr must startWith ("""somecookie=whatever;""")
        hdr must include (";Path=/foo")
      }
    }

    // This is as much a test of ScalatraTests as it is of CookieSupport.
    // http://github.com/scalatra/scalatra/issue/84
    test("handles multiple cookies") {
      session {
        post("/foo/setcookie", Map("cookieval" -> "The value", "anothercookieval" -> "Another Cookie")) {
          body must equal("OK")
        }
        get("/foo/getcookie") {
          body must equal("The value")
          header("X-Another-Cookie") must equal ("Another Cookie")
        }
      }
    }

    test("respects the HttpOnly option") {
      post("/foo/set-http-only-cookie", "cookieval" -> "whatever") {
        val hdr = response.getHeader("Set-Cookie")
        hdr must include (";HttpOnly")
      }
    }

    test("removes a cookie by setting max-age = 0") {
      post("/foo/remove-cookie") {
        val hdr = response.getHeader("Set-Cookie")
        // Jetty turns Max-Age into Expires
        hdr must include (";Expires=Thu, 01-Jan-1970 00:00:00 GMT")
      }
    }

    test("removes a cookie by setting a path") {
      post("/foo/remove-cookie-with-path") {
        val hdr = response.getHeader("Set-Cookie")
        // Jetty turns Max-Age into Expires
        hdr must include (";Expires=Thu, 01-Jan-1970 00:00:00 GMT")
        hdr must include (";Path=/bar")
      }
    }

    test("removing a cookie removes it from the map view") {
      session {
        post("/foo/setcookie") {}
        post("/foo/remove-cookie") {
          header("Somecookie-Is-Defined") must be ("false")
        }
      }
    }
   */
}

