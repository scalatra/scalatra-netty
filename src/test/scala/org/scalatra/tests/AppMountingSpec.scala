package org.scalatra
package tests

import org.specs2.Specification
import java.util.concurrent.atomic.AtomicInteger


class AppMountingSpec extends Specification { def is = 

  "AppMounting should" ^
    "allow mounting an app" ^ 
      "with a basepath" ^
        "starting with a '/'" ! specify.mountsWithBasePathWithSlash ^
        "not starting with a '/'" ! specify.mountsWithBasePathWithoutSlash ^
      "that is a sub app (cfr. ServletContext)" ! pending ^ 
      "that is a scalatra application" ! pending ^
    "when finding applications" ^
      "throw an error when the application can't be found" ! pending ^
      "for an existing application" ^ 
        "find with absolute path" ! pending ^
        "find with a relative path" ! pending ^
        "find for a URI" ! pending ^
  end

  def specify = new AppMountingSpecContext
  val counter = new AtomicInteger()
  class AppMountingSpecContext {
    val mounter = new Mounting {
      def name = "test-mounter-" + counter.incrementAndGet()
    }
    
    def mountsWithBasePathWithSlash = testMount("/somepath", new Mountable {})
    
    def mountsWithBasePathWithoutSlash = testMount("apath", new Mountable {})
    
    private def testMount(path: String,  mountable: Mountable) = {
      val pth = if (!path.startsWith("/")) "/" + path else path
      mounter.mount(path, mountable)
      mounter.applications(pth) must_== mountable
    }
  }
}