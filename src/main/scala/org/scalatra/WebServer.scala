package org.scalatra

import akka.util.Switch
import collection.mutable.ConcurrentMap
import collection.JavaConversions._
import com.google.common.collect.MapMaker
import java.net.URI

object AppMounter {
  type ApplicationRegistry = ConcurrentMap[String, AppMounter]
  def newAppRegistry: ApplicationRegistry = new MapMaker().makeMap[String, AppMounter]
  val EmptyPath = ""
}
trait AppMounter {
  import AppMounter._
  
  private def absolutize(path: String) = normalizePath(if (path.startsWith("/")) path else basePath / pathName / path)
  private[scalatra] def normalizePath(pth: String) = ensureSlash(if (pth.endsWith("/")) pth.dropRight(1) else pth)
  
  private val na = normalizePath _ compose absolutize _
  
  def basePath = "/"
  def pathName = EmptyPath
  def path = na("")
  
  implicit protected def applications: ApplicationRegistry

  def mount(name: String): AppMounter = mount(ServerApp(name, normalizePath(path)))
  def mount(app: AppMounter): AppMounter = mount(app.pathName, app)
  def mount(name: String, app: AppMounter): AppMounter = {
    val normalizedPath = normalizePath(path / name)
    println("Registering an app to: %s" format normalizedPath)
    applications += normalizedPath -> app
    app
  }

  private def ensureSlash(candidate: String) = {
    (candidate.startsWith("/"), candidate.endsWith("/")) match {
      case (true, true) => candidate.dropRight(1)
      case (true, false) => candidate
      case (false, true) => "/" + candidate.dropRight(1)
      case (false, false) => "/" + candidate
    }
  }

  def hasMatchingRoute(req: HttpRequest) = false
  def applicationOption(path: String) = applications get na(path)
  def apply(path: String) = applications(na(path))
  def apply(path: URI) = applications(na(path.getRawPath))
  def unapply(path: String) = applicationOption(path)
}

case class ServerApp(override val pathName: String, override val basePath: String = "/")(implicit val applications: AppMounter.ApplicationRegistry) extends AppMounter

case class ServerInfo(name: String, version: String, port: Int, base: String)
trait WebServer extends AppMounter {

  def name: String
  def version: String
  def port: Int
  def info = ServerInfo(name, version, port, normalizePath(path))

  implicit protected val applications = AppMounter.newAppRegistry
  implicit lazy val appContext = DefaultAppContext(info)

  protected lazy val started = new Switch
  def start()
  def stop()


}