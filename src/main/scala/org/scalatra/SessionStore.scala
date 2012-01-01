//package org.scalatra
//
//import java.util.concurrent.ConcurrentHashMap
//import collection.JavaConversions._
//import com.google.common.collect.MapMaker
//import collection.mutable.{ConcurrentMap, MapProxy}
//
//trait SessionStore[SessionType <: HttpSession] extends MapProxy[String, SessionType] {
//
//  protected def meta: HttpSessionMeta[SessionType]
//  def newSession = {
//    val sess = meta.empty
//    self += sess.id -> sess
//  }
//}
//
//class InMemorySessionStore extends SessionStore[InMemorySession] {
//
//
//  protected def meta = InMemorySession
//
//  val self: ConcurrentMap[String, Any] = (new MapMaker).makeMap[String, Any]
//
//}