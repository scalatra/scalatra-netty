package org.scalatra
package tests

class HttpMethodsApp extends ScalatraApp {
  
  get("/hello/:param") {
    params('param)
  }
  
  get("/query") {
    params('val1) + " " + params('val2)
  }
  
  post("/urlencode") {
    params('first) + " " + params('last)   
  }
  
  post("/multipart") {
    params('first) + " " + params('last)
  }
  
  post("/upload") {
    "uploaded"
  }
  
  put("/update") {
    params("first") + " " + params("last")
  }
  
  put("/mulitpart-update") {
    params("first") + " " + params("last")
  }
  
  put("/update_upload") {
    "uploaded too"
  }
  
  delete("/delete/:id") {
    params("id")
  }
  
  get("/some") { // get also does HEAD
    "head success"
  }
  
  options("/options") {
    "options success"
  }
  
  patch("/patching") {
    "patch success"
  }
  
}

class HttpMethodsSpec extends ScalatraSpec {
  
  mount(new HttpMethodsApp)
  
  def is = 
    "The HttpMethod support should" ^ 
      "allow get requests with path params" ! getHelloParam ^
      "allow get requests with query params" ! getQuery ^
    end
  
  def getHelloParam = {
    get("/hello/world") {
      response.statusCode must_== 200
      response.body must_== "world"
    }
  }

  def getQuery = {
    get("/query", Map("val1" -> "hello", "val2" -> "world")) {
      response.statusCode must_== 200
      response.body must_== "hello world"
    }
  }
}