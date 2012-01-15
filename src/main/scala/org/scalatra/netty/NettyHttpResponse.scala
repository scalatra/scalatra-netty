package org.scalatra
package netty

import io.Codec
import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import scalaz.Scalaz._
import org.jboss.netty.channel.{ChannelFutureListener, ChannelHandlerContext}
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBufferOutputStream}
import org.jboss.netty.handler.codec.http2.{HttpHeaders, DefaultHttpResponse, HttpResponseStatus, HttpVersion => JHttpVersion}

class NettyHttpResponse(request: NettyHttpRequest, connection: ChannelHandlerContext) extends HttpResponse {
  
  private val underlying = new DefaultHttpResponse(nettyProtocol, HttpResponseStatus.OK)
  private def nettyProtocol = request.serverProtocol match {
    case Http10 => JHttpVersion.HTTP_1_0
    case Http11 => JHttpVersion.HTTP_1_1
  }

  def status = underlying.getStatus
  def status_=(status: ResponseStatus) = underlying.setStatus(status)

  def contentType = {
    underlying.getHeader(Names.CONTENT_TYPE).blankOption some identity none {
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
    if(!HttpHeaders.isKeepAlive(underlying) || !chunked) fut.addListener(ChannelFutureListener.CLOSE)
  }

  def chunked = underlying.isChunked

  def chunked_=(chunked: Boolean) = underlying setChunked chunked

  def redirect(uri: String) = {
    underlying.setStatus(HttpResponseStatus.FOUND)
    underlying.setHeader(Names.LOCATION, uri)
    end()
  }
}