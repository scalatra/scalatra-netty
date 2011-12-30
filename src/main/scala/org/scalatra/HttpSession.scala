package org.scalatra

import collection.mutable.{ConcurrentMap}
import collection.{GenTraversableOnce, MapProxyLike}
import collection.JavaConversions._
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import com.google.common.collect.MapMaker

object GenerateId {
  def apply(): String = {
    generate()
  }

  private def hexEncode(bytes: Array[Byte]) =  ((new StringBuilder(bytes.length * 2) /: bytes) { (sb, b) =>
    if((b.toInt & 0xff) < 0x10) sb.append("0")
    sb.append(Integer.toString(b.toInt & 0xff, 16))
  }).toString

  protected def generate() = {
    val tokenVal = new Array[Byte](20)
    (new SecureRandom).nextBytes(tokenVal)
    hexEncode(tokenVal)
  }
}

trait HttpSessionMeta[SessionType <: HttpSession] {
  def empty: SessionType
}
trait HttpSession extends ConcurrentMap[String, Any] with MapProxyLike[String, Any, ConcurrentMap[String, Any]] {
  
  val id = GenerateId()

  protected def newSession(newSelf: ConcurrentMap[String, Any]): HttpSession =
      new HttpSession { val self = newSelf }
  
  override def repr = this
  override def empty: HttpSession = newSession(HttpSession.this.self.empty)
  override def updated(key: String, value: Any) = newSession(self.updated(key, value))

  override def updated [B1 >: Any](key: String, value: B1) = newSession(self.updated(key, value))

  override def + [B1 >: Any] (kv: (String, B1)): HttpSession = newSession(self + kv)
  override def + [B1 >: Any] (elem1: (String, B1), elem2: (String, B1), elems: (String, B1) *) = newSession(self.+(elem1, elem2, elems: _*))
  override def ++[B1 >: Any](xs: GenTraversableOnce[(String, B1)]) = newSession(self ++ xs.seq)
  override def -(key: String) = newSession(self - key)

  override def += (kv: (String, Any)) = { self += kv ; this }
  override def -= (key: String) = { self -= key ; this }

}

object InMemorySession extends HttpSessionMeta[InMemorySession] {
  def empty = new InMemorySession((new MapMaker).makeMap[String, Any])
}
class InMemorySession(val self: ConcurrentMap[String, Any]) extends HttpSession