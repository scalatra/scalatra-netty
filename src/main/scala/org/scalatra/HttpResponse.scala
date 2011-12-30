package org.scalatra

import java.nio.charset.Charset
import org.jboss.netty.handler.codec.http.HttpResponseStatus

object ResponseStatus {
  def apply(nettyStatus: HttpResponseStatus): ResponseStatus = 
    ResponseStatus(nettyStatus.getCode, nettyStatus.getReasonPhrase)
}
case class ResponseStatus(code: Int, message: String) {
  def line = {
    val buf = new StringBuilder(message.length + 5);
    buf.append(code)
    buf.append(' ')
    buf.append(message)
    buf.toString()
  }
}

object HttpResponse {
  val NewLine = sys.props("line.separator")
}

trait HttpResponse {

  import HttpResponse._
  def headers: Map[String, String]
  
  
  var status: ResponseStatus = ResponseStatus(HttpResponseStatus.OK)
  def contentType: String 
  def charset: Charset

  def writeln(message: String, charset: Charset = charset) = write(message + NewLine, charset)
  def write(message: String, charset: Charset = charset)
  def write(bytes: Array[Byte])
  def end()
}