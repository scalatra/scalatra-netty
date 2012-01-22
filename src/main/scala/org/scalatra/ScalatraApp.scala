package org.scalatra

import scala.util.matching.Regex
import java.io.{File, FileInputStream}
import annotation.tailrec
import util._
import scala.io.Codec
import io._
import scala.util.DynamicVariable
import collection.mutable.ListBuffer

trait MultiParamsDef {
  type MultiParams <: Map[String, _ <: Seq[String]]
}

object ScalatraApp extends MultiParamsDef {
  type MultiParams = util.MultiMap

  type Action = () => Any

  val EnvironmentKey = "org.scalatra.environment".intern

  val MultiParamsKey = "org.scalatra.MultiParams".intern
}


trait ScalatraApp extends CoreDsl with Mountable {

  import ScalatraApp._

  
  def isEmpty = false

  def name = getClass.getName

  protected implicit lazy val cookieOptions = CookieOptions(path = appPath)

  override def toString = "ScalatraApp(%s,%s)" format (appPath, name)

  private val submounts = ListBuffer[AppMounter => Any]()
  def initialize(config: AppContext) {
    submounts foreach (_ apply mounter)
    submounts.clear()
  }
  
  protected def mount[TheSubApp <: ScalatraApp](path: String, app: => TheSubApp) {
    if (mounter == null) {
      submounts += { (m: AppMounter) => m.mount(path, app) }
    } else {
      mounter.mount(path, app)
    }
  }

  /**
   * The routes registered in this kernel.
   */
  protected val routes: RouteRegistry = new RouteRegistry

  def hasMatchingRoute(req: HttpRequest) = {
    _request.withValue(req) {
      (routes.matchingMethods flatMap (routes(_)) filter(_().isDefined)).nonEmpty
    }
  }

  /**
   * Executes routes in the context of the current request and response.
   *
   * $ 1. Executes each before filter with `runFilters`.
   * $ 2. Executes the routes in the route registry with `runRoutes` for
   *      the request's method.
   *      a. The result of runRoutes becomes the _action result_.
   *      b. If no route matches the requested method, but matches are
   *         found for other methods, then the `doMethodNotAllowed` hook is
   *         run with each matching method.
   *      c. If no route matches any method, then the `doNotFound` hook is
   *         run, and its return value becomes the action result.
   * $ 3. If an exception is thrown during the before filters or the route
   * $    actions, then it is passed to the `errorHandler` function, and its
   * $    result becomes the action result.
   * $ 4. Executes the after filters with `runFilters`.
   * $ 5. The action result is passed to `renderResponse`.
   */
  protected def executeRoutes() = {
    val result = try {
      runFilters(routes.beforeFilters)
      val actionResult = runRoutes(routes(request.method)).headOption
      actionResult orElse matchOtherMethods() getOrElse doNotFound()
    }
    catch {
      case e: HaltException => renderHaltException(e)
      case e => errorHandler(e)
    }
    finally {
      runFilters(routes.afterFilters)
    }
    renderResponse(result)
  }

  /**
   * Invokes each filters with `invoke`.  The results of the filters
   * are discarded.
   */
  protected def runFilters(filters: Traversable[Route]) =
    for {
      route <- filters
      matchedRoute <- route()
    } invoke(matchedRoute)

  /**
   * Lazily invokes routes with `invoke`.  The results of the routes
   * are returned as a stream.
   */
  protected def runRoutes(routes: Traversable[Route]) =
    for {
      route <- routes.toStream // toStream makes it lazy so we stop after match
      matchedRoute <- route()
      actionResult <- invoke(matchedRoute)
    } yield actionResult

  /**
   * Invokes a route or filter.  The multiParams gathered from the route
   * matchers are merged into the existing route params, and then the action
   * is run.
   *
   * @param matchedRoute the matched route to execute
   *
   * @return the result of the matched route's action wrapped in `Some`,
   * or `None` if the action calls `pass`.
   */
  protected def invoke(matchedRoute: MatchedRoute) =
    withRouteMultiParams(Some(matchedRoute)) {
      try {
        Some(matchedRoute.action())
      }
      catch {
        case e: PassException => None
      }
    }


  def before(transformers: RouteTransformer*)(fun: => Any) =
    routes.appendBeforeFilter(Route(transformers, () => fun))

  def after(transformers: RouteTransformer*)(fun: => Any) =
    routes.appendAfterFilter(Route(transformers, () => fun))

  def notFound(fun: => Any) = doNotFound = { () => fun }

  /**
   * Called if no route matches the current request method, but routes
   * match for other methods.  By default, sends an HTTP status of 405
   * and an `Allow` header containing a comma-delimited list of the allowed
   * methods.
   */
  protected var doMethodNotAllowed: (Set[HttpMethod] => Any) = { allow =>
    status = 405
    response.headers("Allow") = allow.mkString(", ")
  }
  def methodNotAllowed(f: Set[HttpMethod] => Any) = doMethodNotAllowed = f

  private def matchOtherMethods(): Option[Any] = {
    val allow = routes.matchingMethodsExcept(request.method)
    if (allow.isEmpty) None else Some(doMethodNotAllowed(allow))
  }

  /**
   * The error handler function, called if an exception is thrown during
   * before filters or the routes.
   */
  protected var errorHandler: ErrorHandler = { case t => throw t }
  def error(handler: ErrorHandler) = errorHandler = handler orElse errorHandler

  protected def withRouteMultiParams[S](matchedRoute: Option[MatchedRoute])(thunk: => S): S = {
    val originalParams = multiParams
    request(MultiParamsKey) = originalParams ++ matchedRoute.map(_.multiParams).getOrElse(Map.empty)
    try { thunk } finally { request(MultiParamsKey) = originalParams }
  }

  /**
   * Renders the action result to the response.
   * $ - If the content type is still null, call the contentTypeInferrer.
   * $ - Call the render pipeline on the result.
   */
  protected def renderResponse(actionResult: Any) {
    if (contentType == null) {
      contentTypeInferrer.lift(actionResult) foreach { contentType = _ }
    }
    renderResponseBody(actionResult)
  }

  /**
   * A partial function to infer the content type from the action result.
   *
   * @return
   *   $ - "text/plain" for String
   *   $ - "application/octet-stream" for a byte array
   *   $ - "text/html" for any other result
   */
  protected def contentTypeInferrer: ContentTypeInferrer = {
    case _: String => "text/plain"
    case _: Array[Byte] => "application/octet-stream"
    case _ => "text/html"
  }

  /**
   * Renders the action result to the response body via the render pipeline.
   *
   * @see #renderPipeline
   */
  protected def renderResponseBody(actionResult: Any) {
    @tailrec def loop(ar: Any): Any = ar match {
      case _: Unit | Unit =>
      case a => loop(renderPipeline.lift(a) getOrElse ())
    }
    loop(actionResult)
  }

  /**
   * The render pipeline is a partial function of Any => Any.  It is
   * called recursively until it returns ().  () indicates that the
   * response has been rendered.
   */
  protected def renderPipeline: RenderPipeline = {
    case bytes: Array[Byte] =>
      response.outputStream.write(bytes)
    case file: File =>
      using(new FileInputStream(file)) { in => zeroCopy(in, response.outputStream) }
    case _: Unit | Unit =>
      // If an action returns Unit, it assumes responsibility for the response
    case x: Any  =>
      response.outputStream.write(x.toString.getBytes(Codec.UTF8))
  }

  /**
   * The current multiparams.  Multiparams are a result of merging the
   * standard request params (query string or post params) with the route
   * parameters extracted from the route matchers of the current route.
   * The default value for an unknown param is the empty sequence.  Invalid
   * outside `handle`.
   */
  def multiParams: MultiParams = {
    request.attributes.get(MultiParamsKey).get.asInstanceOf[MultiParams]
      .withDefaultValue(Seq.empty)
  }

  /*
   * Assumes that there is never a null or empty value in multiParams.  The servlet container won't put them
   * in request.getParameters, and we shouldn't either.
   */
  protected val _params = new MultiMapHeadView[String, String] with MapWithIndifferentAccess[String] {
    protected def multiMap = multiParams
  }

  /**
   * A view of `multiParams`.  Returns the head element for any known param,
   * and is undefined for any unknown param.  Invalid outside `handle`.
   */
  def params = _params

  /**
   * Pluggable way to convert a path expression to a route matcher.
   * The default implementation is compatible with Sinatra's route syntax.
   *
   * @param path a path expression
   * @return a route matcher based on `path`
   */
  protected implicit def string2RouteMatcher(path: String): RouteMatcher =
    new SinatraRouteMatcher(path, requestPath)

  protected implicit def string2RouteTransformer(path: String): RouteTransformer =
    Route.appendMatcher(path)

  /**
   * Path pattern is decoupled from requests.  This adapts the PathPattern to
   * a RouteMatcher by supplying the request path.
   */
  protected implicit def pathPatternParser2RouteMatcher(pattern: PathPattern): RouteMatcher =
    new PathPatternRouteMatcher(pattern, requestPath)

  protected implicit def pathPattern2RouteTransformer(pattern: PathPattern): RouteTransformer =
    Route.appendMatcher(pattern)

  /**
   * Converts a regular expression to a route matcher.
   *
   * @param regex the regular expression
   * @return a route matcher based on `regex`
   * @see [[org.scalatra.RegexRouteMatcher]]
   */
  protected implicit def regex2RouteMatcher(regex: Regex): RouteMatcher =
    new RegexRouteMatcher(regex, requestPath)

  protected implicit def regex2RouteTransformer(regex: Regex): RouteTransformer =
    Route.appendMatcher(regex)

  /**
   * Converts a boolean expression to a route matcher.
   *
   * @param block a block that evaluates to a boolean
   *
   * @return a route matcher based on `block`.  The route matcher should
   * return `Some` if the block is true and `None` if the block is false.
   *
   * @see [[org.scalatra.BooleanBlockRouteMatcher]]
   */
  protected implicit def booleanBlock2RouteMatcher(block: => Boolean): RouteMatcher =
    new BooleanBlockRouteMatcher(block)

  protected implicit def booleanBlock2RouteTransformer(block: => Boolean): RouteTransformer =
    Route.appendMatcher(block)

  protected implicit def routeMatcher2RouteTransformer(matcher: RouteMatcher): RouteTransformer =
    Route.appendMatcher(matcher)

  protected def renderHaltException(e: HaltException) {
    e match {
      case HaltException(Some(status), Some(reason), _, _) => response.status = ResponseStatus(status, reason)
      case HaltException(Some(status), None, _, _) => response.status = status
      case HaltException(None, _, _, _) => // leave status line alone
    }
    response.headers ++= e.headers
    renderResponse(e.body)
    response.end()
  }

  def get(transformers: RouteTransformer*)(action: => Any) = addRoute(Get, transformers, action)

  def post(transformers: RouteTransformer*)(action: => Any) = addRoute(Post, transformers, action)

  def put(transformers: RouteTransformer*)(action: => Any) = addRoute(Put, transformers, action)

  def delete(transformers: RouteTransformer*)(action: => Any) = addRoute(Delete, transformers, action)

  /**
   * @see [[org.scalatra.ScalatraApp.get]]
   */
  def options(transformers: RouteTransformer*)(action: => Any) = addRoute(Options, transformers, action)

  /**
   * @see [[org.scalatra.ScalatraApp.get]]
   */
  def patch(transformers: RouteTransformer*)(action: => Any) = addRoute(Patch, transformers, action)

  /**
   * Prepends a new route for the given HTTP method.
   *
   * Can be overriden so that subtraits can use their own logic.
   * Possible examples:
   * $ - restricting protocols
   * $ - namespace routes based on class name
   * $ - raising errors on overlapping entries.
   *
   * This is the method invoked by get(), post() etc.
   *
   * @see org.scalatra.ScalatraKernel#removeRoute
   */
  protected def addRoute(method: HttpMethod, transformers: Seq[RouteTransformer], action: => Any): Route = {
    val route = Route(transformers, () => action, () => appPath)
    routes.prependRoute(method, route)
    route
  }

  /**
   * Removes _all_ the actions of a given route for a given HTTP method.
   * If addRoute is overridden then this should probably be overriden too.
   *
   * @see org.scalatra.ScalatraKernel#addRoute
   */
  protected def removeRoute(method: HttpMethod, route: Route): Unit =
    routes.removeRoute(method, route)

  protected def removeRoute(method: String, route: Route): Unit =
    removeRoute(HttpMethod(method), route)

  /**
   * The effective path against which routes are matched.
   */
  def requestPath = {
    PathManipulationOps.ensureSlash(request.path.replaceFirst(appPath, ""))
  }

  /**
   * Called if no route matches the current request for any method.  The
   * default implementation varies between servlet and filter.
   */
  protected var doNotFound: org.scalatra.ScalatraApp.Action = () => {
    response.status = 404
    response.end()
  }

  private val _request = new DynamicVariable[HttpRequest](null)
  private val _response = new DynamicVariable[HttpResponse](null)
  implicit def request = _request.value

  implicit def response = _response.value

  def apply(req: HttpRequest, res: HttpResponse) {
    _request.withValue(req) {
      _response.withValue(res) {
//            // As default, the servlet tries to decode params with ISO_8859-1.
//            // It causes an EOFException if params are actually encoded with the other code (such as UTF-8)
//            if (request. == null)
//              request.setCharacterEncoding(defaultCharacterEncoding)

        val realMultiParams = request.parameters
        //response.charset(defaultCharacterEncoding)
        request(org.scalatra.ScalatraApp.MultiParamsKey) = realMultiParams
        executeRoutes()
        response.end()
      }
    }
  }

  protected def renderStaticFile(file: File) {

  }
}