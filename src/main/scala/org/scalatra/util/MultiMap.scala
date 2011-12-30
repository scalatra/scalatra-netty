package org.scalatra
package util

import scala.collection.immutable.Map
import collection.{MapProxy}

object MultiMap {
  def apply() = new MultiMap
  def apply[SeqType <: Seq[String]](wrapped: Map[String, SeqType]) = new MultiMap(wrapped)

  implicit def map2MultiMap(map: Map[String, Seq[String]]) = new MultiMap(map)
  
}

class MultiMap(val self: Map[String, Seq[String]] = Map.empty) extends MapProxy[String, Seq[String]]  {
  
  override def get(key: String) = self.get(key) orElse self.get(key + "[]")
}
