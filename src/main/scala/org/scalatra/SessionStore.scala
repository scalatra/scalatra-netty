package org.scalatra

import collection.JavaConversions._
import com.google.common.collect.MapMaker
import collection.mutable

trait SessionStore[SessionType <: HttpSession] extends mutable.MapProxy[String, SessionType] {

  protected def meta: HttpSessionMeta[SessionType]
  def newSession = {
    val sess = meta.empty
    self += sess.id -> sess
    sess
  }
}

class InMemorySessionStore extends SessionStore[InMemorySession] {


  protected def meta = InMemorySession

  val self: mutable.ConcurrentMap[String, InMemorySession] = (new MapMaker).makeMap[String, InMemorySession]

}