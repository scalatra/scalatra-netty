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
class ScalatraRequestHandler(serverInfo: ServerInfo) extends SimpleChannelUpstreamHandler {
  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    e.getMessage match {
      case evt: JHttpRequest => {
        val req = new NettyHttpRequest(evt, ensureSlash(serverInfo.base), serverInfo)
        val resp = req newResponse ctx
        val app = new ScalatraApp {
          val requestPath = req.path

          protected var doNotFound: ScalatraApp.Action = { () =>
            resp.status = 404
            resp.end()
          }

          protected def renderStaticFile(file: File) {}

          protected val routeBasePath = "/"

          implicit val appContext = new AppContext {

          }

          implicit val request = req

          implicit val response = resp

          def apply() {
//            // As default, the servlet tries to decode params with ISO_8859-1.
//            // It causes an EOFException if params are actually encoded with the other code (such as UTF-8)
//            if (request. == null)
//              request.setCharacterEncoding(defaultCharacterEncoding)

            val realMultiParams = request.parameterMap
            //response.charset(defaultCharacterEncoding)
            request(ScalatraApp.MultiParamsKey) = realMultiParams
            executeRoutes()
            response.end()
          }

          get("/hello") { "world" }
          get("/") { "OMG it works!" }
        }
        app()
        // select app from mounted applications
        // chop off base path
        // handle the request
      }
    }


  }


  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    Console.err.println(e.getCause.getMessage)
    Console.err.println(e.getCause.getStackTraceString)
    val resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR)
    resp.setContent(ChannelBuffers.copiedBuffer((e.getCause.getMessage + "\n" + e.getCause.getStackTraceString).getBytes(Codec.UTF8)))
    ctx.getChannel.write(resp).addListener(ChannelFutureListener.CLOSE)
  }

  private def ensureSlash(value: String) = if (value startsWith "/") value else "/" + value
}