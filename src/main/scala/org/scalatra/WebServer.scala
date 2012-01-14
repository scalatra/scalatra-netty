package org.scalatra

import akka.util.Switch
import scalax.file._
import com.weiglewilczek.slf4s.Logging
import collection.mutable.ListBuffer

trait PathManipulationOps {
  protected def absolutizePath(path: String): String
  protected def normalizePath(pth: String) = (ensureSlash _ compose absolutizePath _)(pth)

  protected def ensureSlash(candidate: String) = {
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

trait PathManipulation extends PathManipulationOps {
  
  def basePath: String
  def pathName: String
  lazy val appPath: String = absolutizePath(basePath) / pathName
  
  protected def absolutizePath(path: String): String = {
    path.blankOption map (p => ensureSlash(if (p.startsWith("/")) p else appPath / p)) getOrElse appPath
  }
}

case class ServerInfo(name: String, version: String, port: Int, base: String)
object WebServer {
  val DefaultPath = "/"
  val DefaultPathName = ""
}
trait WebServer extends Logging with AppMounterLike {

  def basePath = WebServer.DefaultPath
  def publicDirectory: PublicDirectory

  final val pathName = WebServer.DefaultPathName

  val info = ServerInfo(name, version, port, normalizePath(appPath))
  implicit val appContext = DefaultAppContext(info, AppMounter.newAppRegistry)

  def name: String
  def version: String
  def port: Int
  
  def mount[TheApp <: Mountable](app: => TheApp): AppMounter = mount("/", app)

  protected lazy val started = new Switch
  final def start() {
    started switchOn {
      initializeApps() // If we don't initialize the apps here there are race conditions
      startCallbacks foreach (_.apply())
    }
  }
  
  def initializeApps() {
    applications.values foreach (_.mounted)
  }
  
  private val startCallbacks = ListBuffer[() => Any]()
  private val stopCallbacks = ListBuffer[() => Any]()
  
  def onStart(thunk: => Any) = startCallbacks += { () => thunk }
  def onStop(thunk: => Any) = stopCallbacks += { () => thunk }
  
  final def stop() {
    started switchOff {
      stopCallbacks foreach (_.apply())
    }
  }


}