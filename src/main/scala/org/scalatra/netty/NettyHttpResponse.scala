package org.scalatra
package netty

import io.Codec
import org.jboss.netty.handler.codec.http2.{HttpHeaders, DefaultHttpResponse, HttpResponseStatus, HttpVersion, HttpResponse => JHttpResponse}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import scalaz.Scalaz._
import org.jboss.netty.channel.{ChannelFutureListener, ChannelHandlerContext}
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBufferOutputStream}

class NettyHttpResponse(request: NettyHttpRequest, connection: ChannelHandlerContext) extends HttpResponse {
  
  private implicit def string2richer(s: String) = new {
    def some = if (s == null || s.trim.isEmpty) None else Some(s)
  }
  
  private implicit def respStatus2nettyStatus(stat: ResponseStatus) = new HttpResponseStatus(stat.code, stat.message) 
  
  private val underlying = new DefaultHttpResponse(request.underlying.getProtocolVersion, _status)

  def status = underlying.getStatus
  def status_=(status: ResponseStatus) = underlying.setStatus(status)

  def contentType = {
    underlying.getHeader(Names.CONTENT_TYPE).some some identity none {
      underlying.setHeader(Names.CONTENT_TYPE, "text/plain")
      underlying.getHeader(Names.CONTENT_TYPE)
    }
  }
  def contentType_=(ct: String) = underlying.setHeader(Names.CONTENT_TYPE, ct)
  var charset = Codec.UTF8

  val outputStream  = new ChannelBufferOutputStream(ChannelBuffers.dynamicBuffer())
  def end() = {
    headers foreach { case (k, v) => underlying.addHeader(k, v) }
    underlying.setContent(outputStream.buffer())
    val fut = connection.getChannel.write(underlying)
    if (!HttpHeaders.isKeepAlive(request.underlying)) fut.addListener(ChannelFutureListener.CLOSE)
  }
  
}