package org.scalatra

import java.nio.charset.Charset
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import collection.mutable
import mutable.ConcurrentMap
import com.google.common.collect.MapMaker

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
  def headers: ConcurrentMap[String, String] = new MapMaker().makeMap[String, String]
  
  
  var status: ResponseStatus = ResponseStatus(HttpResponseStatus.OK)
  def contentType: String 
  def contentType_=(ct: String)
  def charset: Charset
  def charset_=(cs: Charset)

  def writeln(message: String, charset: Charset = charset) = write(message + NewLine, charset)
  def write(message: String, charset: Charset = charset)
  def write(bytes: Array[Byte])
  def end()
}