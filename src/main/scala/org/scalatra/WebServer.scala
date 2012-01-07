package org.scalatra

import akka.util.Switch
trait PathManipulation {
  
  def basePath: String
  def pathName: String
  lazy val appPath: String = absolutizePath(basePath) / pathName
  
  protected def absolutizePath(path: String) = na(if (path.startsWith("/")) path else appPath / path)
  protected def normalizePath(pth: String) = (na _ compose absolutizePath _)(pth)

  private[scalatra] def na(pth: String) = ensureSlash(if (pth.endsWith("/")) pth.dropRight(1) else pth)
  
  private def ensureSlash(candidate: String) = {
    (candidate.startsWith("/"), candidate.endsWith("/")) match {
      case (true, true) => candidate.dropRight(1)
      case (true, false) => candidate
      case (false, true) => "/" + candidate.dropRight(1)
      case (false, false) => "/" + candidate
    }
  }

  protected def splitPaths(path: String) = {
    val norm = normalizePath(path)
    val parts = norm split "/"
    (absolutizePath(parts dropRight 1 mkString "/"), parts.lastOption getOrElse "")
  }
  
  
}

case class NullMountable() extends Mountable {

  def isEmpty = true

  def initialize(config: AppContext) {}
  def hasMatchingRoute(req: HttpRequest) = false
}

case class ServerInfo(name: String, version: String, port: Int, base: String)
object WebServer {
  val DefaultPath = "/"
  val DefaultPathName = ""
}
trait WebServer extends AppMounterLike {

  def basePath = WebServer.DefaultPath

  final val pathName = WebServer.DefaultPathName

  val info = ServerInfo(name, version, port, normalizePath(appPath))
  implicit val appContext = DefaultAppContext(info, AppMounter.newAppRegistry)

  def name: String
  def version: String
  def port: Int
  
  def mount[TheApp <: Mountable](app: => TheApp): AppMounter[TheApp] = mount("/", app)

  protected lazy val started = new Switch
  def start()
  def stop()


}