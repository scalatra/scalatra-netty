package org.scalatra
package tests

import org.specs2.Specification
import java.util.concurrent.atomic.AtomicInteger
import java.net.URI

object TestMounter {
  def apply()(implicit applications: AppMounter.ApplicationRegistry) = new TestMounter
}
class TestMounter(implicit val applications: AppMounter.ApplicationRegistry) extends AppMounter
class AppMounterSpec extends Specification { def is =

  "AppMounting should" ^
    "allow mounting an app" ^ 
      "with a basepath" ^
        "starting with a '/'" ! specify.mountsWithBasePathWithSlash ^
        "not starting with a '/'" ! specify.mountsWithBasePathWithoutSlash ^ bt(2) ^
    "when finding applications" ^ t ^
      "throw an error when the application can't be found" ! specify.throwForNonExisting ^ bt ^
      "for an existing application" ^ 
        "find with absolute path from root mounter" ! specify.findsAbsolutePathFromRoot ^
        "find with absolute path from sub mounter" ! specify.findsAbsolutePathFromSub ^
        "find with a relative path" ! specify.findsRelativePath ^
        "find for an absolute URI" ! specify.findsForAbsoluteUri ^
        "find for a relative URI" ! specify.findsForRelativeUri ^
  end

  def specify = new AppMountingSpecContext
  val counter = new AtomicInteger()

  class AppMountingSpecContext {

    implicit val applicationRegistry = AppMounter.newAppRegistry

    val mounter = new AppMounter {
      def name = "test-mounter-" + counter.incrementAndGet()

      implicit val applications = applicationRegistry
    }
    
    def mountsWithBasePathWithSlash = testMount("/somepath", TestMounter())
    
    def mountsWithBasePathWithoutSlash = testMount("apath", TestMounter())
    
    def throwForNonExisting = {
      mounter.mount("thepath", TestMounter()) 
      mounter("i-don-t-exist") must throwA[NoSuchElementException]
    }
    
    def findsAbsolutePathFromRoot = {
      val posts = mounter.mount("posts")
      val comments = posts.mount("comments")
      mounter("/posts/comments") must_== comments
    }
    
    def findsAbsolutePathFromSub = {
      val posts = mounter.mount("posts")
      val comments = posts.mount("comments")
      comments("/posts/comments") must_== comments
    }

    def findsRelativePath = {
      val posts = mounter.mount("posts")
      val comments = posts.mount("comments")
      posts("comments") must_== comments
    }
    
    def findsForAbsoluteUri = {
      val posts = mounter.mount("posts")
      val comments = posts.mount("comments")
      posts(URI.create("/posts/comments")) must_== comments
    }
    
    def findsForRelativeUri = {
      val posts = mounter.mount("posts")
      val comments = posts.mount("comments")
      posts(URI.create("comments")) must_== comments
    }

    private def testMount(path: String,  mountable: AppMounter) = {
      val pth = if (!path.startsWith("/")) "/" + path else path
      mounter.mount(path, mountable)
      mounter(pth) must_== mountable
    }
  }
}