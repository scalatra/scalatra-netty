package org.scalatra
package netty

import org.jboss.netty.channel.ChannelHandler.Sharable
import org.jboss.netty.handler.codec.http2.{HttpRequest => JHttpRequest}
import org.jboss.netty.channel.{MessageEvent, ChannelHandlerContext, SimpleChannelUpstreamHandler}

/**
 * This handler is shared across the entire server, providing application level settings
 */
@Sharable
class ScalatraApplicationHandler(serverInfo: ServerInfo) extends SimpleChannelUpstreamHandler {

//  private val sessions = new InMemorySessionStore()
//
//  override def channelOpen(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
////    ctx.setAttachment(sessions.newSession)
//  }
//
//  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
//    e.getMessage match {
//      case req: JHttpRequest => {
//      }
//    }
//  }
}
