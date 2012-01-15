package org.scalatra
package netty

import io.Codec
import org.jboss.netty.channel._
import util.MultiMap
import collection.JavaConversions._
import scalaz._
import Scalaz._
import org.jboss.netty.handler.codec.http2.HttpHeaders.Names
import org.jboss.netty.handler.codec.http2.InterfaceHttpData.HttpDataType
import org.jboss.netty.buffer.{ChannelBufferFactory, ChannelBuffers, ChannelBufferInputStream}
import org.jboss.netty.handler.codec.frame.TooLongFrameException
import org.jboss.netty.handler.codec.http2.{HttpChunkTrailer, HttpVersion => JHttpVersion, HttpHeaders, FileUpload, Attribute, QueryStringDecoder, HttpChunk, DefaultHttpDataFactory, HttpPostRequestDecoder, HttpRequest => JHttpRequest}
import java.net.{SocketAddress, URI}

class ScalatraRequestBuilder(maxPostBodySize: Long = 2097152)(implicit val appContext: AppContext) extends ScalatraUpstreamHandler {

  @volatile private var request: JHttpRequest = _
  @volatile private var method: HttpMethod = _
  private val factory = new DefaultHttpDataFactory()
  private var postDecoder: Option[HttpPostRequestDecoder] = None

  private def clearDecoder() = {
    postDecoder foreach (_.cleanFiles())
    postDecoder = None
  } 
  
  override def channelClosed(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
    clearDecoder()
  }
  
  private def isHtmlPost = {
    val ct = request.getHeader(Names.CONTENT_TYPE).blankOption
    method.allowsBody && ct.forall(t => t.startsWith("application/x-www-form-urlencoded") || t.startsWith("multipart/form-data"))
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    e.getMessage match {
      case request: JHttpRequest => {
        clearDecoder()
        this.request = request
        this.method = request.getMethod
        if (isHtmlPost)
          postDecoder = new HttpPostRequestDecoder(factory, request, Codec.UTF8).some
        
        if (!request.isChunked) sendOn(e.getChannel, e.getRemoteAddress)
        else {
          mangleTransferEncodingHeaders()
          if(!isHtmlPost) initializeChunkedBody(e.getChannel.getConfig.getBufferFactory)
        }
      }
      case chunk: HttpChunk => {
        if (isHtmlPost)
          postDecoder foreach { _ offer chunk }
        else
          addChunkToBody(chunk)
        
        if (chunk.isLast) {
          addTrailingHeaders(chunk)
          if (!isHtmlPost) request.setHeader(Names.CONTENT_LENGTH, request.getContent.readableBytes().toString)
          sendOn(e.getChannel, e.getRemoteAddress)
        }
      }
      case _ => ctx sendUpstream e
    }
  }
  
  private def sendOn(channel: Channel, remoteAddress: SocketAddress) {
    val req = scalatraRequest
    request = null
    Channels.fireMessageReceived(channel, req, remoteAddress)
  }
  
  private def addTrailingHeaders(chunk: HttpChunk) {
    chunk match {
      case trailer: HttpChunkTrailer => {
        trailer.getHeaders foreach { h =>
          request.setHeader(h.getKey, h.getValue)
        }
      }
      case _ =>
    }
  }
  
  private def mangleTransferEncodingHeaders() {
    val encodings = request.getHeaders(Names.TRANSFER_ENCODING)
    encodings remove HttpHeaders.Values.CHUNKED
    if (encodings.isEmpty) request.removeHeader(Names.TRANSFER_ENCODING)
    else request.setHeader(Names.TRANSFER_ENCODING, encodings)
  }
  
  private def initializeChunkedBody(factory: ChannelBufferFactory) {
    request setChunked false
    request setContent ChannelBuffers.dynamicBuffer(factory)    
  }
  
  private def addChunkToBody(chunk: HttpChunk) {
    if ((request.getContent.readableBytes() + chunk.getContent.readableBytes()) > maxPostBodySize) {
      throw new TooLongFrameException("HTTP content length exceeded " + maxPostBodySize + " bytes.")
    }
    request.getContent.writeBytes(chunk.getContent)
  }
  
  private def scalatraRequest: HttpRequest = {
    if (isHtmlPost) {
      val (parameters, files) = (postDecoder map readPostData) | (MultiMap(), Map.empty[String, HttpFile])
      new NettyHttpRequest(
        method,
        URI.create(request.getUri),
        headers,
        queryString,
        parameters,
        files,
        serverProtocol,
        new ChannelBufferInputStream(ChannelBuffers.buffer(0)))
    } else {
      new NettyHttpRequest(
        method,
        URI.create(request.getUri),
        headers,
        queryString,
        MultiMap(),
        Map.empty,
        serverProtocol,
        inputStream)
    }
  }
  
  private def queryString = new QueryStringDecoder(request.getUri).getParameters.mapValues(_.toSeq): MultiMap
  
  private def headers = Map((request.getHeaders map { e => e.getKey -> e.getValue.blankOption.orNull }):_*)
  
  private def inputStream = new ChannelBufferInputStream(request.getContent)
  
  private def serverProtocol = request.getProtocolVersion match {
    case JHttpVersion.HTTP_1_0 => Http10
    case JHttpVersion.HTTP_1_1 => Http11
  }
  
  private def defaultMultiMap = MultiMap().withDefaultValue(Seq.empty)
  private def readPostData(decoder: HttpPostRequestDecoder): (Map[String, Seq[String]], Map[String, HttpFile]) = {
    decoder.getBodyHttpDatas.foldLeft((defaultMultiMap, Map.empty[String, HttpFile])) { (acc, data) =>
      val (container, files) = acc
      data match {
        case d: Attribute if d.getHttpDataType == HttpDataType.Attribute => {
          (container + (d.getName -> (Seq(d.getValue) ++ container(d.getName))), files)
        }
        case d: FileUpload if d.getHttpDataType == HttpDataType.FileUpload => {
          (container, files + (d.getName -> new NettyHttpFile(d)))
        }
        case _ => acc
      }
    }
  }

}