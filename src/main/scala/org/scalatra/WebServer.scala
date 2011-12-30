package org.scalatra

import akka.util.Switch
import collection.mutable.ConcurrentMap
import com.google.common.collect.MapMaker

trait Mountable

trait Mounting {
  def base = "/"
  def path = base + name
  def name: String

  def mount(name: String, app: Mountable)
  lazy val applications: ConcurrentMap[String, Mountable] = new MapMaker().makeMap[String, Mountable]
}

trait SubApp extends Mountable with Mounting {

}

case class ServerInfo(name: String, version: String, port: Int, base: String, applications: ConcurrentMap[String, Mountable])
trait WebServer extends Mounting {
  
  def version: String
  def port: Int

  lazy val started = new Switch
  def start()
  def stop()
  
  
  def mount(name: String, app: Mountable)
  
  def info = ServerInfo(name, version, port, base, applications)

}