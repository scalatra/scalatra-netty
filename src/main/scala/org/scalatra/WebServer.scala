package org.scalatra

import akka.util.Switch
import collection.mutable.ConcurrentMap
import collection.JavaConversions._
import com.google.common.collect.MapMaker

object Mounting {
  type ApplicationRegistry = ConcurrentMap[String, Mounting]
  def newAppRegistry: ApplicationRegistry = new MapMaker().makeMap[String, Mounting]
  val EmptyPath = ""
}
trait Mounting {
  import Mounting._
  def basePath = "/"
  def pathName = EmptyPath
  def path = normalizePath(basePath / pathName)
  implicit def applications: ApplicationRegistry

  def mount(name: String): Mounting = mount(ServerApp(name, normalizePath(path)))
  def mount(app: Mounting): Mounting = mount(app.pathName, app)
  def mount(name: String, app: Mounting): Mounting = { applications += normalizePath(path / name) -> app; app }

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

case class ServerApp(override val pathName: String, override val basePath: String = "/")(implicit val applications: Mounting.ApplicationRegistry) extends Mounting

case class ServerInfo(name: String, version: String, port: Int, base: String)
trait WebServer extends Mounting {

  def name: String
  def version: String
  def port: Int

  protected lazy val started = new Switch
  def start()
  def stop()


  implicit val applications = Mounting.newAppRegistry

  def info = ServerInfo(name, version, port, normalizePath(path))

}