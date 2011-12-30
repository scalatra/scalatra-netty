package org.scalatra
package netty

import collection.mutable.ConcurrentMap
import org.jboss.netty.channel.{MessageEvent, ChannelHandlerContext, SimpleChannelUpstreamHandler}
import org.jboss.netty.handler.codec.http.{QueryStringEncoder, QueryStringDecoder, HttpHeaders, HttpRequest => JHttpRequest, HttpMethod => JHttpMethod}

/**
 * This handler is akin to the handle method of scalatra
 */
class ScalatraRequestHandler(serverInfo: ServerInfo) extends SimpleChannelUpstreamHandler {
  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    e.getMessage match {
      case evt: JHttpRequest => {
        val req = new NettyHttpRequest(evt, ensureSlash(serverInfo.base), serverInfo)
      }
    }
  }
  
  private def ensureSlash(value: String) = if (value startsWith "/") value else "/" + value
}