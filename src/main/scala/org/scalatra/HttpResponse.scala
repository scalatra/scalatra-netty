package org.scalatra

import java.nio.charset.Charset
import org.jboss.netty.handler.codec.http2.HttpResponseStatus
import collection.mutable
import mutable.ConcurrentMap
import com.google.common.collect.MapMaker
import java.io.OutputStream
import collection.JavaConversions._

object ResponseStatus {
  def apply(nettyStatus: HttpResponseStatus): ResponseStatus = 
    ResponseStatus(nettyStatus.getCode, nettyStatus.getReasonPhrase)
}
case class ResponseStatus(code: Int, message: String) extends Ordered[ResponseStatus] {

  def compare(that: ResponseStatus) = code.compareTo(that.code)

  def line = {
    val buf = new StringBuilder(message.length + 5);
    buf.append(code)
    buf.append(' ')
    buf.append(message)
    buf.toString()
  }
}

trait HttpResponse {

  lazy val headers: ConcurrentMap[String, String] = new MapMaker().makeMap[String, String]

  def status: ResponseStatus
  def status_=(status: ResponseStatus)
  def contentType: String
  def contentType_=(ct: String)
  def charset: Charset
  def charset_=(cs: Charset)
  def chunked: Boolean
  def chunked_=(chunked: Boolean)

  def outputStream: OutputStream

  def redirect(uri: String)

  def end()
}