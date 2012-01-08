package org.scalatra
package netty

import org.jboss.netty.handler.codec.http2.{HttpVersion, HttpResponseStatus, DefaultHttpResponse, HttpRequest => JHttpRequest, HttpResponse => JHttpResponse}
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.channel._
import scala.io.Codec
import com.weiglewilczek.slf4s.Logging

/**
 * This handler is akin to the handle method of scalatra
 */
class ScalatraRequestHandler(implicit val appContext: AppContext) extends SimpleChannelUpstreamHandler with Logging {

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    
    e.getMessage match {
      case evt: JHttpRequest => {
        logger debug ("Received request to: %s" format evt.getUri)
        val req = new NettyHttpRequest(evt, ensureSlash(appContext.server.base))
        val resp = req newResponse ctx
        val app = appContext.application(req)
        if (app.isDefined) {
          app.get(req, resp)
        } else {
          logger debug  ("Couldn't match the request")
          val resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)
          ctx.getChannel.write(resp).addListener(ChannelFutureListener.CLOSE)
        }
      }
    }


  }


  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    try {
      logger error ("Caught an exception", e.getCause)
      val resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR)
      resp.setContent(ChannelBuffers.copiedBuffer((e.getCause.getMessage + "\n" + e.getCause.getStackTraceString).getBytes(Codec.UTF8)))
      ctx.getChannel.write(resp).addListener(ChannelFutureListener.CLOSE)
    } catch {
      case e => {
        logger error ("Error during error handling", e)
        //ctx.getChannel.close().await()
      }
    }
  }

  private def ensureSlash(value: String) = if (value startsWith "/") value else "/" + value
}