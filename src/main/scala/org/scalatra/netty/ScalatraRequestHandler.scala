package org.scalatra
package netty

import collection.mutable.ConcurrentMap
import java.io.File
import util.MultiMap
import org.jboss.netty.handler.codec.http2.{HttpVersion, HttpResponseStatus, DefaultHttpResponse, HttpRequest => JHttpRequest, HttpResponse => JHttpResponse}
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.channel._
import scala.io.Codec

/**
 * This handler is akin to the handle method of scalatra
 */
class ScalatraRequestHandler(implicit val appContext: AppContext) extends SimpleChannelUpstreamHandler {
  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    e.getMessage match {
      case evt: JHttpRequest => {
        val req = new NettyHttpRequest(evt, ensureSlash(appContext.server.base))
        val resp = req newResponse ctx
        val app = appContext.application(req)
        if (app.isDefined) {
          app.get(req, resp)
        } else {
          Console.println("Couldn't match the request")
          val resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)
          ctx.getChannel.write(resp).addListener(ChannelFutureListener.CLOSE)
        }
      }
    }


  }


  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    try {
      Console.err.println(e.getCause.getMessage)
      Console.err.println(e.getCause.getStackTraceString)
      val resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR)
      resp.setContent(ChannelBuffers.copiedBuffer((e.getCause.getMessage + "\n" + e.getCause.getStackTraceString).getBytes(Codec.UTF8)))
      ctx.getChannel.write(resp).addListener(ChannelFutureListener.CLOSE)
    } catch {
      case _ => {
        Console.err.println("Error during error handling")
        //ctx.getChannel.close().await()
      }
    }
  }

  private def ensureSlash(value: String) = if (value startsWith "/") value else "/" + value
}