package org.scalatra

import collection.mutable.ConcurrentMap
import com.google.common.collect.MapMaker
import collection.JavaConversions._
import scalaz._
import Scalaz._

trait AppContext {

  implicit def applications: AppMounter.ApplicationRegistry
  def server: ServerInfo
  lazy val attributes: ConcurrentMap[String, Any] = new MapMaker().makeMap[String, Any]() 
  
  def get(key: String) = attributes.get(key)
  def apply(key: String) = attributes(key)
  def update(key: String, value: Any) = attributes(key) = value
  
  def application(req: HttpRequest): Option[ScalatraApp] = {
    Console.println("The registered applications:")
    Console.println(applications)
    application(req.uri.getPath) map (_.mounted) flatMap {
      case f: ScalatraApp if f.hasMatchingRoute(req) => {
        Console.println("We found an App")
        f.some
      }
      case f: ScalatraApp => {
        Console.println("We found an App, But no matching route")
        none[ScalatraApp]
      }
      case _ => {
        Console.println("No matching route")
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
      Console.println("The current path: %s" format curr)
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

case class DefaultAppContext(server: ServerInfo, applications: AppMounter.ApplicationRegistry) extends AppContext