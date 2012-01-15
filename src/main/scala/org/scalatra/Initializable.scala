package org.scalatra

import util.PathManipulation

/**
 * Trait representing an object that can't be fully initialized by its
 * constructor.  Useful for unifying the initialization process of an
 * HttpServlet and a Filter.
 */
trait Initializable { self: PathManipulation =>

  /**
   * A hook to initialize the class with some configuration after it has
   * been constructed.
   */
  def initialize(config: AppContext)
}
