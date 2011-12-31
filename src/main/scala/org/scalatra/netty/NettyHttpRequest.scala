package org.scalatra
package netty

import org.jboss.netty.handler.codec.http2.HttpHeaders.Names
import scalaz.Scalaz._
import collection.JavaConversions._
import util.MultiMap
import org.jboss.netty.buffer.{ChannelBufferInputStream}
import org.jboss.netty.handler.codec.http2.{ HttpPostRequestDecoder, CookieDecoder, Cookie => JCookie, QueryStringDecoder, HttpRequest => JHttpRequest, HttpMethod => JHttpMethod}
import org.jboss.netty.channel.ChannelHandlerContext

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
  private implicit def string2richer(s: String) = new {
    def some = if (s == null || s.trim.isEmpty) None else Some(s)
  }
  
  
  private val queryStringDecoder = new QueryStringDecoder(underlying.getUri)
  private val parsedUri = ParsedUri(underlying.getUri)
  val method: HttpMethod = underlying.getMethod


  val path = queryStringDecoder.getPath.replace("^/" + appPath, "")

  val headers = {
    Map((underlying.getHeaders map { e => e.getKey -> e.getValue.some.orNull }):_*)
    
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

  val contentLength =
    headers.get(Names.CONTENT_LENGTH) flatMap (Option(_)) some (_.toLong) none { if (isWsHandshake) 8L else 0L }

  val serverName = serverInfo.name

  val serverPort = serverInfo.port

  val serverProtocol = underlying.getProtocolVersion.getText

  val inputStream = new ChannelBufferInputStream(underlying.getContent)

  private val postDecoder = new HttpPostRequestDecoder(underlying)
  val parameterMap = {
    if (!method.allowsBody) {
      queryString
    } else {
      if (postDecoder.isMultipart) {

      }
    }
  }

  def newResponse(ctx: ChannelHandlerContext) = new NettyHttpResponse(request, ctx)
}