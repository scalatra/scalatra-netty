package org.scalatra
package tests

import java.util.Locale
import Locale.ENGLISH
import java.nio.charset.Charset
import scalax.io.{Codec => Codecx, Resource}
import collection.JavaConversions._
import java.net.URI
import rl.MapQueryString
import scala.io.Codec
import org.jboss.netty.handler.codec.http2.HttpHeaders.Names
import java.io.{File, InputStream}
import com.ning.http.client._
import util.{FileCharset, Mimes}
import java.lang.Throwable
import scalaz._
import Scalaz._

object StringHttpMethod {
  val GET = "GET"
  val POST = "POST"
  val DELETE = "DELETE"
  val PUT = "PUT"
  val CONNECT = "CONNECT"
  val HEAD = "HEAD"
  val OPTIONS = "OPTIONS"  
  val PATCH = "PATCH"  
  val TRACE = "TRACE"
}

abstract class ClientResponse {
  
  def status: ResponseStatus
  def contentType: String  
  def inputStream: InputStream
  def cookies: Map[String, org.scalatra.HttpCookie]
  def headers: Map[String, String] 
  def uri: URI
  
  private var _body: String = null
  
  def statusCode = status.code
  def statusText = status.line
  def body = {
    if (_body == null) _body = Resource.fromInputStream(inputStream).slurpString(Codecx(nioCharset))
    _body
  }

  private def nioCharset = charset some Charset.forName none Codec.UTF8
  def mediaType: Option[String] = headers.get("Content-Type") map { _.split(";")(0) }

  def charset: Option[String] =
    for {
      ct <- headers.get("Content-Type")
      charset <- ct.split(";").drop(1).headOption
    } yield { charset.toUpperCase.replace("CHARSET=", "").trim }
}

class NettyClientResponse(response: Response) extends ClientResponse {
  val cookies = (response.getCookies map { cookie =>
    val cko = CookieOptions(cookie.getDomain, cookie.getPath, cookie.getMaxAge)
    cookie.getName -> org.scalatra.Cookie(cookie.getName, cookie.getValue)(cko)
  }).toMap

  val headers = (response.getHeaders.keySet() map { k => k -> response.getHeaders(k).mkString("; ")}).toMap

  val status = ResponseStatus(response.getStatusCode, response.getStatusText)

  val contentType = response.getContentType

  val inputStream = response.getResponseBodyAsStream

  val uri = response.getUri
}

class NettyClient(val host: String, val port: Int) extends Client {

  import StringHttpMethod._
  private val clientConfig = new AsyncHttpClientConfig.Builder().setFollowRedirects(false).build()
  private val underlying = new AsyncHttpClient(clientConfig) {
    def preparePatch(uri: String): AsyncHttpClient#BoundRequestBuilder = requestBuilder("PATCH", uri)
    def prepareTrace(uri: String): AsyncHttpClient#BoundRequestBuilder = requestBuilder("TRACE", uri)
  }

  override def stop() {
    underlying.close()
  }

  private def requestFactory(method: String): String ⇒ AsyncHttpClient#BoundRequestBuilder = {
    method.toUpperCase(ENGLISH) match {
      case `GET`     ⇒ underlying.prepareGet _
      case `POST`    ⇒ underlying.preparePost _
      case `PUT`     ⇒ underlying.preparePut _
      case `DELETE`  ⇒ underlying.prepareDelete _
      case `HEAD`    ⇒ underlying.prepareHead _
      case `OPTIONS` ⇒ underlying.prepareOptions _
      case `CONNECT` ⇒ underlying.prepareConnect _
      case `PATCH`   ⇒ underlying.preparePatch _
      case `TRACE`   ⇒ underlying.prepareTrace _
    }
  }
  
  private def addParameters(method: String, params: Iterable[(String, String)], isMultipart: Boolean = false, charset: Charset = Codec.UTF8)(req: AsyncHttpClient#BoundRequestBuilder) = {
    method.toUpperCase(ENGLISH) match {
      case `GET` | `DELETE` | `HEAD` | `OPTIONS` ⇒ params foreach { case (k, v) ⇒ req addQueryParameter (k, v) }
      case `PUT` | `POST`   | `PATCH`            ⇒ {
        if (!isMultipart)
          params foreach { case (k, v) ⇒ req addParameter (k, v) }
        else {
          params foreach { case (k, v) => req addBodyPart new StringPart(k, v, charset.name)}
        }
      }
      case _                                     ⇒ // we don't care, carry on
    }
    req
  }
  
  private def addHeaders(headers: Map[String, String])(req: AsyncHttpClient#BoundRequestBuilder) = {
    headers foreach { case (k, v) => req.setHeader(k, v) }
    req
  }
  
  private val allowsBody = Vector(PUT, POST, PATCH)

  def submit[A](method: String, uri: String, params: Iterable[(String, String)], headers: Map[String, String], files: Seq[File], body: String)(f: => A) = {
    val u = URI.create(uri)
    val isMultipart = {
      allowsBody.contains(method.toUpperCase(Locale.ENGLISH)) && {
        val ct = (defaultWriteContentType(files) ++ headers)(Names.CONTENT_TYPE)
        ct.toLowerCase(Locale.ENGLISH).startsWith("multipart/form-data")
      }
    } 
    val reqUri = if (u.isAbsolute) u else new URI("http", null, host, port, u.getPath, u.getQuery, u.getFragment)
    val req = (requestFactory(method)
      andThen (addHeaders(headers) _)
      andThen (addParameters(method, params, isMultipart) _))(reqUri.toASCIIString)
    if (isMultipart) {
      files foreach { file =>
        req.addBodyPart(new FilePart(file.getName, file, Mimes(file), FileCharset(file).name))
      }
    } 
    if (useSession && cookies.size > 0) {
      cookies foreach { cookie =>
        val ahcCookie = new Cookie(
          cookie.cookieOptions.domain,
          cookie.name, cookie.value,
          cookie.cookieOptions.path,
          cookie.cookieOptions.maxAge,
          cookie.cookieOptions.secure)
        req.addCookie(ahcCookie)
      }
    }
    u.getQuery.blankOption foreach { uu =>  
      MapQueryString.parseString(uu) foreach { case (k, v) => v foreach { req.addQueryParameter(k, _) } }
    }
    if (allowsBody.contains(method.toUpperCase(ENGLISH)) && body.nonBlank) req.setBody(body)
    val res = req.execute(async).get
    withResponse(res)(f)
  }
  

  
  private def async = new AsyncCompletionHandler[ClientResponse] {

    override def onThrowable(t: Throwable) {
      t.printStackTrace()
    }

    def onCompleted(response: Response) = new NettyClientResponse(response)
  }
}