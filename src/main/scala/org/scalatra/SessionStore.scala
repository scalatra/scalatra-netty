package org.scalatra

import collection.mutable.MapProxy
import java.util.concurrent.ConcurrentHashMap
import collection.JavaConversions._
import com.google.common.collect.MapMaker

trait SessionStore[SessionType <: HttpSession] extends MapProxy[String, SessionType] {

  protected def meta: HttpSessionMeta[SessionType]
  def newSession = {
    val sess = meta.empty
    self += sess.id -> sess
  }
}

class InMemorySessionStore extends SessionStore[InMemorySession] {

  val self = (new MapMaker).makeMap[String, Any]

}