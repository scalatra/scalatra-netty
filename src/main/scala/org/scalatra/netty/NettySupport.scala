package org.scalatra
package netty

import java.io.File
import scala.util.DynamicVariable

trait NettySupport extends ScalatraApp with Initializable {

  type Config = AppContext

  private var config: AppContext = null

  implicit def appContext = config



  protected def renderStaticFile(file: File) {

  }

}