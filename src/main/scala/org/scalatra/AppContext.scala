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

//  implicit def sessions: SessionStore

}

case class DefaultAppContext(server: ServerInfo)(implicit val applications: AppMounter.ApplicationRegistry) extends AppContext