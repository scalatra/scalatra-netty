package org.scalatra

import akka.util.Switch
import collection.mutable.ConcurrentMap
import collection.JavaConversions._
import com.google.common.collect.MapMaker

object Mounting {
  type ApplicationRegistry = ConcurrentMap[String, Mounting]
  def newAppRegistry: ApplicationRegistry = new MapMaker().makeMap[String, Mounting]
}
trait Mounting {
  import Mounting._
  def basePath = "/"
  var name = ""
  def path = normalizePath(basePath / name)
  def mount(app: Mounting): Mounting = { applications += path -> app; app }

  private def ensureSlash(candidate: String) = {
    (candidate.startsWith("/"), candidate.endsWith("/")) match {
      case (true, true) => candidate.dropRight(1)
      case (true, false) => candidate
      case (false, true) => "/" + candidate.dropRight(1)
      case (false, false) => "/" + candidate
    }
  }
  
  def normalizePath(pth: String) = ensureSlash(if (pth.endsWith("/")) pth.dropRight(1) else pth)
  
}

case class ServerApp(name: String,  basePath: String = "/")(implicit applications: Mounting.ApplicationRegistry) extends Mounting

case class ServerInfo(name: String, version: String, port: Int, base: String)
trait WebServer extends Mounting {
  
  def version: String
  def port: Int

  protected lazy val started = new Switch
  def start()
  def stop()


  implicit val applications = Mounting.newAppRegistry

  def mount(name: String): Mounting = mount(ServerApp(name, normalizePath(basePath)))
  
  def info = ServerInfo(name, version, port, base)

}