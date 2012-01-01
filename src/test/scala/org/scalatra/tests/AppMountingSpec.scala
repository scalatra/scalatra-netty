package org.scalatra
package tests

import org.specs2.Specification
import java.util.concurrent.atomic.AtomicInteger

object TestMounter {
  def apply()(implicit applications: Mounting.ApplicationRegistry) = new TestMounter
}
class TestMounter(implicit val applications: Mounting.ApplicationRegistry) extends Mounting
class AppMountingSpec extends Specification { def is = 

  "AppMounting should" ^
    "allow mounting an app" ^ 
      "with a basepath" ^
        "starting with a '/'" ! specify.mountsWithBasePathWithSlash ^
        "not starting with a '/'" ! specify.mountsWithBasePathWithoutSlash ^ bt(2) ^
    "when finding applications" ^ t ^
      "throw an error when the application can't be found" ! pending ^ bt ^
      "for an existing application" ^ 
        "find with absolute path" ! pending ^
        "find with a relative path" ! pending ^
        "find for a URI" ! pending ^
  end

  def specify = new AppMountingSpecContext
  val counter = new AtomicInteger()
  class AppMountingSpecContext {
    implicit val applicationRegistry = Mounting.newAppRegistry
    val mounter = new Mounting {
      def name = "test-mounter-" + counter.incrementAndGet()

      implicit val applications = applicationRegistry
    }
    
    def mountsWithBasePathWithSlash = testMount("/somepath", TestMounter())
    
    def mountsWithBasePathWithoutSlash = testMount("apath", TestMounter())
    
    private def testMount(path: String,  mountable: Mounting) = {
      val pth = if (!path.startsWith("/")) "/" + path else path
      mounter.mount(path, mountable)
      applicationRegistry(pth) must_== mountable
    }
  }
}