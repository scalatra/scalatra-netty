package org.scalatra

import collection.mutable.ConcurrentMap
import com.google.common.collect.MapMaker
import collection.JavaConversions._
import scalaz._
import Scalaz._
import com.weiglewilczek.slf4s.Logging
import scalax.file.Path

trait AppContext extends Logging {

  implicit def applications: AppMounter.ApplicationRegistry
  def server: ServerInfo
  lazy val attributes: ConcurrentMap[String, Any] = new MapMaker().makeMap[String, Any]() 
  
  def get(key: String) = attributes.get(key)
  def apply(key: String) = attributes(key)
  def update(key: String, value: Any) = attributes(key) = value
  
  def application(req: HttpRequest): Option[ScalatraApp] = {
    logger.trace("The registered applications:")
    logger.trace("%s" format applications)
    application(req.uri.getPath) map (_.mounted) flatMap {
      case f: ScalatraApp if f.hasMatchingRoute(req) => {
        logger.trace("We found an App")
        f.some
      }
      case f: ScalatraApp => {
        logger.trace("We found an App, But no matching route")
        none[ScalatraApp]
      }
      case _ => {
        logger.trace("No matching route")
        none[ScalatraApp]
      }
    }
  }
  
  def application(path: String): Option[AppMounter] = {
    val parts = path.split("/")
    if (parts.length == 0) applications get "/"
    else {
      var i = 1
      var curr = "" / parts(i)
      var next: Option[AppMounter] = applications get curr
      var app: Option[AppMounter] = applications get "/"
      while (app.isDefined && next.isDefined) {
        i += 1
        app = next
        next = if((i) < parts.length) {
          curr = curr / parts(i)
          applications get curr
        } else None
      }
      app
    }
  }

}

case class PublicDirectory(path: Path, cacheFiles: Boolean = true)
case class DirectoryInfo(public: PublicDirectory, temp: Path, data: Path)
case class DefaultAppContext(server: ServerInfo, applications: AppMounter.ApplicationRegistry) extends AppContext with PathManipulationOps {
  protected def absolutizePath(path: String) = ensureSlash(if (path.startsWith("/")) path else server.base / path)
}