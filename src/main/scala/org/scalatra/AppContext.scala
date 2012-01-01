package org.scalatra

import collection.mutable.ConcurrentMap
import com.google.common.collect.MapMaker
import collection.JavaConversions._

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
    val path = req.uri.getPath
    val parts = path.split("/")
    val found = if (parts.length == 0) applications get "/"
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
    if (found.isDefined) {
      Console.println("We found an App")
      if (found.get.isInstanceOf[ScalatraApp] && found.get.hasMatchingRoute(req)) {
        found.map(_.asInstanceOf[ScalatraApp])
      } else {
        Console.println("But no matching route")
        None
      }
    } else None
    //found filter (_.hasMatchingRoute(req)) map (_.asInstanceOf[ScalatraApp])
  }

//  implicit def sessions: SessionStore

}

case class DefaultAppContext(server: ServerInfo)(implicit val applications: AppMounter.ApplicationRegistry) extends AppContext