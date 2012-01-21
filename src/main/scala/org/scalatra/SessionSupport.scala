package org.scalatra

import scalaz._
import Scalaz._

/**
 * Marker trait to enable session support in an app.
 */
trait SessionSupport {
  private var _session: Option[HttpSession] = None
  def session: HttpSession = _session | SessionsDisableException()

  private[scalatra] def session_=(session: HttpSession) = {
    require(session != null, "The session can't be null")
    _session = session.some
    session
  }

}
