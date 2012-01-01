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
    Console.println("The path: " + path)
    val parts = path.split("/")
    Console.println("This has %d parts" format parts.length)
    val found = if (parts.length == 0) applications get "/"
      else {
        var i = 1
        var curr = "" / parts(i)
        Console.println("The current path: %s" format curr)
        var next: Option[AppMounter] = applications get parts(i)
        Console.println("Next is defined: %s" format next.isDefined)
        var app: Option[AppMounter] = applications get "/"
        Console.println("App is defined: %s" format app.isDefined)
        while (app.isDefined && next.isDefined && (i + 1) < parts.length) {
          curr = curr / parts(i)
          Console.println("The current path: %s" format curr)
          app = next
          next = applications get parts(i + 1)
          Console.println("Next is defined: %s" format next.isDefined)
          Console.println("App is defined: %s" format app.isDefined)
          i += 1
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