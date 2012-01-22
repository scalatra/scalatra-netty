package org.scalatra

import akka.event.Logging

trait ScalatraLogging {
  implicit def appContext: AppContext

  protected lazy val logger = Logging(appContext.actorSystem, getClass)

}
