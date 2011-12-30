package org.scalatra
package netty

import org.jboss.netty.channel.{Channels, ChannelPipelineFactory}
import org.jboss.netty.handler.codec.http.{HttpResponseEncoder, HttpChunkAggregator, HttpRequestDecoder}


class ScalatraPipelineFactory(serverInfo: ServerInfo) extends ChannelPipelineFactory {
  def getPipeline = {
    val pipe = Channels.pipeline()
    pipe.addLast("decoder", new HttpRequestDecoder)
    pipe.addLast("aggregator", new HttpChunkAggregator(8912))
    pipe.addLast("encoder", new HttpResponseEncoder)
    pipe.addLast("sessions", new ScalatraApplicationHandler(serverInfo))
    pipe.addLast("handler", new ScalatraRequestHandler(serverInfo))
    pipe
  }
}