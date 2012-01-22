package org.scalatra
package tests

import xml.Text
import java.nio.charset.Charset

class ContentTypeTestApp extends ScalatraApp {
  get("/json") {
    contentType = "application/json; charset=utf-8"
    """{msg: "test"}"""
  }

  get("/html") {
    contentType = "text/html; charset=utf-8"
    "test"
  }

  get("/implicit/string") {
    "test"
  }

  get("/implicit/string/iso-8859-1") {
    response.charset = Charset.forName("iso-8859-1")
    "test"
  }

  get("/implicit/byte-array") {
    "test".getBytes
  }

  get("/implicit/text-element") {
    Text("test")
  }

//  import Actor._
//  val conductor = actor {
//    loop {
//      reactWithin(10000) {
//        case 1 =>
//          val firstSender = sender
//          reactWithin(10000) {
//            case 2 =>
//              firstSender ! 1
//            case 'exit =>
//              exit()
//            case TIMEOUT =>
//              firstSender ! "timed out"
//            }
//        case 'exit =>
//          exit()
//        case TIMEOUT =>
//          sender ! "timed out"
//      }
//    }
//  }

//  get("/concurrent/1") {
//    contentType = "1"
//    // Wait for second request to complete
//    (conductor !! 1)()
//  }
//
//  get("/concurrent/2") {
//    contentType = "2"
//    // Let first request complete
//    conductor ! 2
//  }
//
  get("/default-charset") {
    contentType = "text/xml"
  }

  post("/echo") {
    params("echo")
  }


}

class ContentTypeSpec extends ScalatraSpec {
  mount(new ContentTypeTestApp)

  def is = //sequential ^
    "To support content types the app should" ^
      "correctly tag a json response" ! jsonContentType ^
      "correctly tag a html response" ! htmlContentType ^
      "contentType of a string defaults to text/plain" ! stringDefaultsToPlain ^
      "contentType of a byte array defaults to application/octet-stream" ! bytesDefault ^
      "contentType of a text element defaults to text/html" ! textElementDefaultsHtml ^
      "implicit content type does not override charset" ! noImplicitCharsetOverride ^
//      "charset is set to default when only content type is explicitly set" ! fallsbackDefaultCharset ^
    end

  def jsonContentType = {
    get("/json") {
      response.mediaType must beSome("application/json")
    }
  }

  def htmlContentType = {
    get("/html") {
      response.mediaType must beSome("text/html")
    }
  }

  def stringDefaultsToPlain = {
    get("/implicit/string") {
      response.mediaType must beSome("text/plain")
    }
  }

  def bytesDefault = {
    get("/implicit/byte-array") {
      response.mediaType must beSome("application/octet-stream")
    }
  }

  def textElementDefaultsHtml = {
    get("/implicit/text-element") {
      response.mediaType must beSome("text/html")
    }
  }

  def noImplicitCharsetOverride = {
    get("/implicit/string/iso-8859-1") {
      response.charset must beSome("ISO-8859-1")
    }
  }

  def fallsbackDefaultCharset = {
    get("/default-charset") {
      response.charset must beSome("UTF-8")
    }
  }

/*
  test("contentType is threadsafe") {
    import Actor._

    def doRequest = actor {
      loop {
        react {
          case i: Int =>
            val req = new HttpTester
            req.setVersion("HTTP/1.0")
            req.setMethod("GET")
            req.setURI("/concurrent/"+i)
            // Execute in own thread in servlet with LocalConnector
            val conn = tester.createLocalConnector()
            val res = new HttpTester
            res.parse(tester.getResponses(req.generate(), conn))
            sender ! (i, res.mediaType)
            exit()
        }
      }
    }

    val futures = for (i <- 1 to 2) yield { doRequest !! i }
    for (future <- futures) {
      val result = future() match {
        case (i, mediaType) => mediaType should be (Some(i.toString))
      }
    }
  }

  test("does not override request character encoding when explicitly set") {
    val charset = "iso-8859-5"
    val message = "Здравствуйте!"

    val req = new HttpTester("iso-8859-1")
    req.setVersion("HTTP/1.0")
    req.setMethod("POST")
    req.setURI("/echo")
    req.setHeader("Content-Type", "application/x-www-form-urlencoded; charset="+charset)
    req.setContent("echo="+URLEncoder.encode(message, charset))
    println(req.generate())

    val res = new HttpTester("iso-8859-1")
    res.parse(tester.getResponses(req.generate()))
    println(res.getCharacterEncoding)
    res.getContent should equal(message)
  }*/
}
