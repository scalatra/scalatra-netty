package org.scalatra
package netty

import org.jboss.netty.channel.{Channels, ChannelPipelineFactory}
import org.jboss.netty.handler.codec.http2.{HttpResponseEncoder, HttpChunkAggregator, HttpRequestDecoder}


class ScalatraPipelineFactory(implicit val applicationContext: AppContext) extends ChannelPipelineFactory {

  private val applicationHandler = new ScalatraApplicationHandler

  def getPipeline = {
    val pipe = Channels.pipeline()
    pipe.addLast("decoder", new HttpRequestDecoder)
    pipe.addLast("aggregator", new HttpChunkAggregator(16 * 1024))
    pipe.addLast("encoder", new HttpResponseEncoder)
    pipe.addLast("sessions", applicationHandler)
    pipe.addLast("handler", new ScalatraRequestHandler)
    pipe
  }
}