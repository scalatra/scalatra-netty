package org.scalatra
package netty

import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import scalaz.Scalaz._
import collection.JavaConversions._
import util.MultiMap
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBufferInputStream}
import org.jboss.netty.handler.codec.http.{HttpRequestDecoder, CookieDecoder, Cookie => JCookie, QueryStringDecoder, HttpHeaders, HttpRequest => JHttpRequest, HttpMethod => JHttpMethod}

private object ParsedUri {
  private val UriParts = """^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?""".r
  def apply(uriString: String): ParsedUri = {
    val UriParts(_, sch, _, auth, rawPath, _, query, _, fragment) = uriString
    ParsedUri(sch, auth, rawPath, query, fragment)
  }
}
private case class ParsedUri(scheme: String, authority: String, rawPath: String, queryString: String, fragment: String)

class NettyHttpRequest(val underlying: JHttpRequest, val appPath: String, val serverInfo: ServerInfo) extends HttpRequest {
  private implicit def jHttpMethod2HttpMethod(orig: JHttpMethod): HttpMethod = orig match {
    case JHttpMethod.CONNECT => Connect
    case JHttpMethod.DELETE => Delete
    case JHttpMethod.GET => Get
    case JHttpMethod.HEAD => Head
    case JHttpMethod.OPTIONS => Options
    case JHttpMethod.PATCH => Patch
    case JHttpMethod.POST => Post
    case JHttpMethod.PUT => Put
    case JHttpMethod.TRACE => Trace
  }
  private implicit def nettyCookieToRequestCookie(orig: JCookie) =
    RequestCookie(orig.getName, orig.getValue, CookieOptions(orig.getDomain, orig.getPath, orig.getMaxAge, comment = orig.getComment))
  
  private val queryStringDecoder = new QueryStringDecoder(underlying.getUri)
  private val parsedUri = ParsedUri(underlying.getUri)
  val method: HttpMethod = underlying.getMethod


  val path = queryStringDecoder.getPath.replace("^/" + appPath, "")

  val headers = {
    Map((underlying.getHeaders map { e => e.getKey -> e.getValue }):_*)
    
  }

  val scheme = parsedUri.scheme

  val cookies = {
    val nettyCookies = new CookieDecoder(true).decode(headers.getOrElse(Names.COOKIE, ""))
    val requestCookies = 
      Map((nettyCookies map { nc =>
        val reqCookie: RequestCookie = nc
        reqCookie.name -> reqCookie
      }).toList:_*)
    new CookieJar(requestCookies)
  }

  val queryString = new MultiMap(queryStringDecoder.getParameters)

  val contentType = headers(Names.CONTENT_TYPE)

  private def isWsHandshake =
    method == Get && headers.contains(Names.SEC_WEBSOCKET_KEY1) && headers.contains(Names.SEC_WEBSOCKET_KEY2)

  val contentLength = headers.get(Names.CONTENT_LENGTH) some (_.toLong) none { if (isWsHandshake) 8L else 0L }

  val serverName = serverInfo.name

  val serverPort = serverInfo.port

  val serverProtocol = underlying.getProtocolVersion.getText

  val inputStream = new ChannelBufferInputStream(underlying.getContent)

  val parameterMap = {
    val postDecoder = new HttpRequestDecoder
  }
}