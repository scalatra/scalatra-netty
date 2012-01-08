package org.scalatra.netty

import org.jboss.netty.channel.{MessageEvent, ChannelHandlerContext, SimpleChannelUpstreamHandler}
import org.jboss.netty.handler.codec.http2.{HttpVersion, HttpResponseStatus, DefaultHttpResponse, HttpRequest => JHttpRequest, HttpResponse => JHttpResponse}

class StaticFileHandler extends SimpleChannelUpstreamHandler {
  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    e.getMessage match {
      case nettyReq: JHttpRequest => {
        val reqPath = nettyReq.getUri
      }
    }
  }
}