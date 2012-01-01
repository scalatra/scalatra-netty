package org.scalatra
package netty

import java.io.File
import scala.util.DynamicVariable

class ScalatraApp(override val basePath: String = "/")(implicit val appContext: AppContext) extends org.scalatra.ScalatraApp {

  protected def renderStaticFile(file: File) {

  }

  
}