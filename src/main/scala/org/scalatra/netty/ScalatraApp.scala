package org.scalatra
package netty

import java.io.File
import scala.util.DynamicVariable

class ScalatraApp(override val basePath: String = "/")(implicit val appContext: AppContext) extends org.scalatra.ScalatraApp {

  implicit val applications = appContext.applications

  override var pathName = ""
  def requestPath = request.path

  protected var doNotFound: org.scalatra.ScalatraApp.Action = () => {
    response.status = 404
    response.end()
  }

  protected def renderStaticFile(file: File) {

  }

  private val _request = new DynamicVariable[HttpRequest](null)
  private val _response = new DynamicVariable[HttpResponse](null)
  implicit var request = _request.value

  implicit var response = _response.value
  
  def apply(req: HttpRequest, res: HttpResponse) {
    request = req
    response = res
//            // As default, the servlet tries to decode params with ISO_8859-1.
//            // It causes an EOFException if params are actually encoded with the other code (such as UTF-8)
//            if (request. == null)
//              request.setCharacterEncoding(defaultCharacterEncoding)

    val realMultiParams = request.parameterMap
    //response.charset(defaultCharacterEncoding)
    request(org.scalatra.ScalatraApp.MultiParamsKey) = realMultiParams
    executeRoutes()
    response.end()
  }
  
}