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
    ""
}