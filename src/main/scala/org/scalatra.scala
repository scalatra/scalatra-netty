package org



package object scalatra extends Control {
  type RouteTransformer = (Route => Route)

  type ErrorHandler = PartialFunction[Throwable, Any]

  type ContentTypeInferrer = PartialFunction[Any, String]

  type RenderPipeline = PartialFunction[Any, Any]

}