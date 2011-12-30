package org.scalatra
package netty

import org.jboss.netty.util.internal.ConcurrentHashMap
import java.util.concurrent.Executors
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.group.DefaultChannelGroup
import org.jboss.netty.handler.codec.http.{HttpRequestDecoder, HttpChunkAggregator, HttpResponseEncoder}
import org.jboss.netty.channel.{ChannelPipelineFactory, Channels}
import java.net.InetSocketAddress

class NettyServer extends WebServer {


  protected val applications = new ConcurrentHashMap[String, Mountable]
  val bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()))
  bootstrap setPipelineFactory channelFactory

  val allChannels = new DefaultChannelGroup()

  def channelFactory = new ScalatraPipelineFactory(info)
  
  def start() = started switchOn {
    bootstrap.bind(new InetSocketAddress(port))
  }

  def port = 8765

  def stop = started switchOff {
    allChannels.close().awaitUninterruptibly()
    bootstrap.releaseExternalResources()
  }

  def mount(name: String, app: Mountable) = null
}