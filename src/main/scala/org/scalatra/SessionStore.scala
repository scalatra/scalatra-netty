package org.scalatra

import collection.mutable
import org.jboss.netty.util.{HashedWheelTimer, TimerTask, Timeout}
import akka.util.Duration
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.TimeUnit
import com.google.common.collect.MapMaker
import collection.JavaConversions._

trait SessionStore[SessionType <: HttpSession] extends mutable.Map[String, SessionType] with mutable.MapLike[String,  SessionType, SessionStore[SessionType]] {

  protected def meta: HttpSessionMeta[SessionType]
  def newSession: SessionType

  def newSessionWithId(id: String): SessionType

  def stop()
  def invalidateAll()

  def invalidate(session: HttpSession): this.type = invalidate(session.id)
  def invalidate(sessionId: String): this.type

  override def empty: this.type = emptyStore.asInstanceOf[this.type]
  protected def emptyStore: this.type
}


object InMemorySessionStore {
  
  private case class Entry[T](value: T, expiration: Duration) {
    private val lastAccessed = new AtomicLong(System.currentTimeMillis)
    def isExpired  = lastAccessed.get < (System.currentTimeMillis - expiration.toMillis)
    def expire() = lastAccessed.set(0L)
    def tickAccess() = lastAccessed.set(System.currentTimeMillis)
  }
  
}

/**
 * crude and naive non-blocking LRU implementation that expires sessions after the specified timeout
 * @param appContext
 */
class InMemorySessionStore(implicit appContext: AppContext) extends SessionStore[InMemorySession] with mutable.MapLike[String,  InMemorySession, InMemorySessionStore] {

  
  import InMemorySessionStore.Entry
  protected def meta = InMemorySession
  
  private val scavenger = new HashedWheelTimer()
  private var timeout = expireSessions

  private val self: mutable.ConcurrentMap[String, Entry[InMemorySession]] =
    (new MapMaker).makeMap[String, Entry[InMemorySession]]()


  def emptyStore = new InMemorySessionStore().asInstanceOf[this.type]

  override def seq = self map { case (k, v) => (k -> v.value) }

  def get(key: String) = {
    self get key filterNot (_.isExpired) map (_.value)
  }

  def newSession = {
    val sess = meta.empty
    self += sess.id -> Entry(sess, appContext.sessionTimeout)
    sess
  }

  def newSessionWithId(id: String) = {
    val sess = meta.emptyWithId(id)
    self += sess.id -> Entry(sess, appContext.sessionTimeout)
    sess
  }


  def invalidate(sessionId: String) = this -= sessionId

  def invalidateAll() = self.clear()

  def iterator = {
    val o = self.iterator filterNot (_._2.isExpired)
    new Iterator[(String, InMemorySession)] {
      def hasNext = o.hasNext 

      def next() = {
        val nxt = o.next()
        nxt._2.tickAccess()
        (nxt._1, nxt._2.value)
      }
    }
  }
  
  private def expireSessions: Timeout = scavenger.newTimeout(new TimerTask {
    def run(timeout: Timeout) {
      self.valuesIterator filter (_.isExpired) foreach { self -= _.value.id }
      InMemorySessionStore.this.timeout = expireSessions
    }
  }, 1, TimeUnit.SECONDS)

  def +=(kv: (String, InMemorySession)) = {
    self += kv._1 -> Entry(kv._2, appContext.sessionTimeout)
    this
  }

  def -=(key: String) = {
    self -= key
    this
  }

  def stop() {
    if (timeout != null && !timeout.isExpired) timeout.cancel()
    scavenger.stop()
  }
}