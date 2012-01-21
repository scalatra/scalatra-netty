package org

import scalatra.util.MultiMap
import java.util.regex.Pattern
import rl.UrlCodingUtils
import com.google.common.base.{Function => GuavaFunction}


package object scalatra extends Control {
  type RouteTransformer = (Route => Route)

  type ErrorHandler = PartialFunction[Throwable, Any]

  type ContentTypeInferrer = PartialFunction[Any, String]

  type RenderPipeline = PartialFunction[Any, Any]

  implicit def map2MultiMap(map: scala.collection.Map[String, Seq[String]]): MultiMap = new MultiMap(Map(map.toSeq:_*))
  
  implicit def appMounter2app(appMounter: AppMounter): Mountable = appMounter.mounted
  implicit def app2AppMounter(app: Mountable): AppMounter = app.mounter.asInstanceOf[AppMounter]

  implicit def extendedByteArray(bytes: Array[Byte]) = new {
    def hexEncode =  ((new StringBuilder(bytes.length * 2) /: bytes) { (sb, b) =>
        if((b.toInt & 0xff) < 0x10) sb.append("0")
        sb.append(Integer.toString(b.toInt & 0xff, 16))
      }).toString
  }
  
  implicit def extendedString(s: String) = new {

    def blankOption = if (isBlank) None else Some(s)
    def isBlank = s == null || s.trim.isEmpty
    def nonBlank = s != null && s.trim.nonEmpty

    def urlEncode = UrlCodingUtils.urlEncode(s)
    def formEncode = UrlCodingUtils.urlEncode(s, spaceIsPlus = true)
    def urlDecode = UrlCodingUtils.urlDecode(s)
    def formDecode = UrlCodingUtils.urlDecode(s, plusIsSpace = true)

    def /(path: String) = (s.endsWith("/"), path.startsWith("/")) match {
      case (true, false) | (false, true) ⇒ s + path
      case (false, false)                ⇒ s + "/" + path
      case (true, true)                  ⇒ s + path substring 1
    }

    def regexEscape = Pattern.quote(s)

  }
  
  implicit def scalaFunction2GoogleFunction[I, O](fn: I => O) =
    new GuavaFunction[I,  O] { def apply(input: I) = fn(input) }
  
  implicit def int2StatusCode(code: Int) = code match {
    case 100 => ResponseStatus(100, "Continue")
    case 101 => ResponseStatus(101, "Switching Protocols")
    case 102 => ResponseStatus(102, "Processing")
    case 200 => ResponseStatus(200, "OK")
    case 201 => ResponseStatus(201, "Created")
    case 202 => ResponseStatus(202, "Accepted")
    case 203 => ResponseStatus(203, "Non-Authoritative Information")
    case 204 => ResponseStatus(204, "No Content")
    case 205 => ResponseStatus(205, "Reset Content")
    case 206 => ResponseStatus(206, "Partial Content")
    case 207 => ResponseStatus(207, "Multi-Status")
    case 300 => ResponseStatus(300, "Multiple Choices")
    case 301 => ResponseStatus(301, "Moved Permanently")
    case 302 => ResponseStatus(302, "Found")
    case 303 => ResponseStatus(303, "See Other")
    case 304 => ResponseStatus(304, "Not Modified")
    case 305 => ResponseStatus(305, "Use Proxy")
    case 307 => ResponseStatus(307, "Temporary Redirect")
    case 400 => ResponseStatus(400, "Bad Request")
    case 401 => ResponseStatus(401, "Unauthorized")
    case 402 => ResponseStatus(402, "Payment Required")
    case 403 => ResponseStatus(403, "Forbidden")
    case 404 => ResponseStatus(404, "Not Found")
    case 405 => ResponseStatus(405, "Method Not Allowed")
    case 406 => ResponseStatus(406, "Not Acceptable")
    case 407 => ResponseStatus(407, "Proxy Authentication Required")
    case 408 => ResponseStatus(408, "Request Timeout")
    case 409 => ResponseStatus(409, "Conflict")
    case 410 => ResponseStatus(410, "Gone")
    case 411 => ResponseStatus(411, "Length Required")
    case 412 => ResponseStatus(412, "Precondition Failed")
    case 413 => ResponseStatus(413, "Request Entity Too Large")
    case 414 => ResponseStatus(414, "Request-URI Too Long")
    case 415 => ResponseStatus(415, "Unsupported Media Type")
    case 416 => ResponseStatus(416, "Requested Range Not Satisfiable")
    case 417 => ResponseStatus(417, "Expectation Failed")
    case 422 => ResponseStatus(422, "Unprocessable Entity")
    case 423 => ResponseStatus(423, "Locked")
    case 424 => ResponseStatus(424, "Failed Dependency")
    case 425 => ResponseStatus(425, "Unordered Collection")
    case 426 => ResponseStatus(426, "Upgrade Required")
    case 500 => ResponseStatus(500, "Internal Server Error")
    case 501 => ResponseStatus(501, "Not Implemented")
    case 502 => ResponseStatus(502, "Bad Gateway")
    case 503 => ResponseStatus(503, "Service Unavailable")
    case 504 => ResponseStatus(504, "Gateway Timeout")
    case 505 => ResponseStatus(505, "HTTP Version Not Supported")
    case 506 => ResponseStatus(506, "Variant Also Negotiates")
    case 507 => ResponseStatus(507, "Insufficient Storage")
    case 510 => ResponseStatus(510, "Not Extended")
  }
}