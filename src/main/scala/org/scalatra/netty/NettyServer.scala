package org.scalatra
package netty

import java.util.concurrent.Executors
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.group.DefaultChannelGroup
import java.net.InetSocketAddress
import org.jboss.netty.util.Version

class NettyServer extends WebServer {


  def name = "ScalatraNettyServer"

  def version = Version.ID

  private val bossThreadPool = Executors.newCachedThreadPool()
  private val workerThreadPool = Executors.newCachedThreadPool()
  val bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossThreadPool, workerThreadPool))
  bootstrap setPipelineFactory channelFactory

  val allChannels = new DefaultChannelGroup()

  def channelFactory = new ScalatraPipelineFactory()
  
  def start() = started switchOn {
    bootstrap.bind(new InetSocketAddress(port))
  }

  val port = 8765

  def stop = started switchOff {
    allChannels.close().awaitUninterruptibly()
    bootstrap.releaseExternalResources()
    workerThreadPool.shutdown()
    bossThreadPool.shutdown()
  }

}