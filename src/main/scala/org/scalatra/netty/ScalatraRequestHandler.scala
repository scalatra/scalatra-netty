package org.scalatra
package netty

import collection.mutable.ConcurrentMap
import org.jboss.netty.channel.{MessageEvent, ChannelHandlerContext, SimpleChannelUpstreamHandler}
import org.jboss.netty.handler.codec.http2.{HttpRequest => JHttpRequest}

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