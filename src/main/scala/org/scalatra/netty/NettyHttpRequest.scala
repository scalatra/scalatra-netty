package org.scalatra
package netty

import org.jboss.netty.handler.codec.http2.HttpHeaders.Names
import scalaz.Scalaz._
import collection.JavaConversions._
import util.MultiMap
import org.jboss.netty.buffer.{ChannelBufferInputStream}
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.handler.codec.http2.{Attribute, HttpPostRequestDecoder, QueryStringDecoder, HttpRequest => JHttpRequest, HttpMethod => JHttpMethod}
import org.jboss.netty.handler.codec.http.{CookieDecoder, Cookie => JCookie}

private object ParsedUri {
  private val UriParts = """^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?""".r
  def apply(uriString: String): ParsedUri = {
    val UriParts(_, sch, _, auth, rawPath, _, query, _, fragment) = uriString
    ParsedUri(sch, auth, rawPath, query, fragment)
  }
}
private case class ParsedUri(scheme: String, authority: String, rawPath: String, queryString: String, fragment: String)

class NettyHttpRequest(val underlying: JHttpRequest, val appPath: String, val serverInfo: ServerInfo) extends HttpRequest {


  private val queryStringDecoder = new QueryStringDecoder(underlying.getUri)
  private val parsedUri = ParsedUri(underlying.getUri)
  val method: HttpMethod = underlying.getMethod


  val path = queryStringDecoder.getPath.replace("^/" + appPath, "")

  val headers = {
    Map((underlying.getHeaders map { e => e.getKey -> e.getValue.blank.orNull }):_*)
    
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

  val queryString = {
    queryStringDecoder.getParameters.mapValues(_.toSeq): MultiMap
  }

  val contentType = headers.get(Names.CONTENT_TYPE).flatMap(_.blank)

  private def isWsHandshake =
    method == Get && headers.contains(Names.SEC_WEBSOCKET_KEY1) && headers.contains(Names.SEC_WEBSOCKET_KEY2)

  private def wsZero = if (isWsHandshake) 8L.some else 0L.some
  val contentLength =
    headers get Names.CONTENT_LENGTH flatMap (_.blank some (_.toLong.some) none wsZero)

  val serverName = serverInfo.name

  val serverPort = serverInfo.port

  val serverProtocol = underlying.getProtocolVersion.getText

  val inputStream = new ChannelBufferInputStream(underlying.getContent)

//  private val postDecoder = new HttpPostRequestDecoder(underlying)
  val parameterMap = {
    queryString
//    if (!method.allowsBody) {
//      queryString
//    } else {
//      if (postDecoder.isMultipart) {
//        postDecoder.getBodyHttpDatas map { data =>
//          data.getHttpDataType match {
//            case formData: Attribute => {
//              data.getName -> Seq(formData.getValue)
//            }
//            case
//          }
//        }
//      } else {
//
//      }
//    }
  }

  private[scalatra] def newResponse(ctx: ChannelHandlerContext) = new NettyHttpResponse(this, ctx)
}