package org.scalatra

import java.net.URI
import com.google.common.collect.MapMaker
import collection.mutable.ConcurrentMap
import collection.JavaConversions._

trait Mountable extends PathManipulation with Initializable {

  @volatile private[scalatra] var mounter: AppMounter[_ <: Mountable] = _
  def basePath = mounter.basePath
  def pathName = mounter.pathName
  implicit def appContext: AppContext = mounter.appContext

  def isEmpty: Boolean
  def isDefined: Boolean = !isEmpty
  def hasMatchingRoute(req: HttpRequest): Boolean


}

trait AppMounterLike extends PathManipulation {
  implicit def appContext: AppContext
  
  def applications = appContext.applications
  def get(path: String) = appContext.application(normalizePath(path))
  def apply(path: String): AppMounter[_ <: Mountable] = get(path).get
  def apply(path: URI): AppMounter[_ <: Mountable] = apply(normalizePath(path.getRawPath))

  protected def ensureApps(base: String): AppMounter[_ <: Mountable] = {
    val pth = normalizePath(base)
    Console.out.println("the base path: " + pth)
    applications.get(pth) getOrElse {
      val (parent, _) = splitPaths(pth)
      val app = if (pth == "/") {
        new AppMounter("/", "", NullMountable())
      } else {
        ensureApps(parent)
      }
      applications(pth) = app
      app
    }
  }


  def mount[TheSubApp <: Mountable](path: String, app: => TheSubApp): AppMounter[TheSubApp] = {
    val (longest, name) = splitPaths(path)
    val parent: AppMounter[_ <: Mountable] = ensureApps(longest)
    var curr = applications.get(parent.appPath / name)
    if (curr forall (_.isEmpty)) {
      curr = Some(new AppMounter[TheSubApp](parent.appPath, name, app))
      applications(parent.appPath / name) = curr.get
    }
    curr.get.asInstanceOf[AppMounter[TheSubApp]]
  }
}
object AppMounter {
  type ApplicationRegistry = ConcurrentMap[String, AppMounter[_ <: Mountable]]
  def newAppRegistry: ApplicationRegistry = new MapMaker().makeMap[String, AppMounter[_ <: Mountable]]
}
final class AppMounter[TheApp <: Mountable](val basePath: String, val pathName: String, app: => TheApp)(implicit val appContext: AppContext) extends AppMounterLike {
  lazy val mounted = {
    val a = app
    a.mounter = this
    a.initialize(appContext)
    a
  }

  type This = TheApp

  def mount(path: String): AppMounter[NullMountable] = {
    val (longest, name) = splitPaths(path)
    val parent: AppMounter[_ <: Mountable] = ensureApps(longest)
    mount(parent.appPath / name, NullMountable())
  }

  override def toString = "MountedApp(%s)" format mounted.getClass.getName
}
