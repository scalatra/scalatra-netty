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
trait AppMounter extends Initializable {
  import AppMounter._



  private def absolutize(path: String) = normalizePath(if (path.startsWith("/")) path else basePath / pathName / path)
  private[scalatra] def normalizePath(pth: String) = ensureSlash(if (pth.endsWith("/")) pth.dropRight(1) else pth)
  
  private val na = normalizePath _ compose absolutize _
  
  def basePath = "/"
  def pathName = EmptyPath
  def path = na("")

  implicit def appContext: AppContext
  implicit protected def applications = appContext.applications

  def mount(name: String)(implicit appContext: AppContext): AppMounter = mount(ServerApp(name, normalizePath(path)))
  def mount(name: String, app: AppMounter)(implicit appContext: AppContext): AppMounter = {
    val normalizedPath = normalizePath(path / name)
    println("Registering an app to: %s" format normalizedPath)
    app initialize appContext
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

case class ServerApp(override val pathName: String, override val basePath: String = "/")(implicit val applications: AppMounter.ApplicationRegistry) extends AppMounter {
  def initialize(config: AppContext) {}
}

case class ServerInfo(name: String, version: String, port: Int, base: String)
trait WebServer extends AppMounter {

  def initialize(config: AppContext) {}

  def name: String
  def version: String
  def port: Int
  def info = ServerInfo(name, version, port, normalizePath(path))

  implicit protected val applications = AppMounter.newAppRegistry
  implicit val appContext = DefaultAppContext(info)

  def mount(app: AppMounter)(implicit appContext: AppContext): AppMounter = mount("", app)

  protected lazy val started = new Switch
  def start()
  def stop()


}