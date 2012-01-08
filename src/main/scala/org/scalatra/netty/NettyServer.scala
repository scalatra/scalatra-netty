package org.scalatra
package netty

import java.util.concurrent.Executors
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.group.DefaultChannelGroup
import java.net.InetSocketAddress
import org.jboss.netty.util.Version


case class NettyServer(
             name: String = "ScalatraNettyServer", 
             version: String = Version.ID , 
             port: Int = 8765, 
             publicDirectory: PublicDirectory = PublicDirectory("public"),
             override val basePath: String = "/") extends WebServer {


  private val bossThreadPool = Executors.newCachedThreadPool()
  private val workerThreadPool = Executors.newCachedThreadPool()
  private val bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossThreadPool, workerThreadPool))
  bootstrap setPipelineFactory channelFactory

  val allChannels = new DefaultChannelGroup()

  def channelFactory = new ScalatraPipelineFactory()
  
  onStart {
    logger info ("Starting Netty HTTP server on %d" format port)
    bootstrap.bind(new InetSocketAddress(port))
  }

  onStop {
    allChannels.close().awaitUninterruptibly()
    bootstrap.releaseExternalResources()
    workerThreadPool.shutdown()
    bossThreadPool.shutdown()
    logger info ("Netty HTTP server on %d stopped." format port)
  }

}