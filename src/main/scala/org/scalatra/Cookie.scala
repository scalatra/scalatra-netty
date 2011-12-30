package org.scalatra

import collection._
import java.util.Locale
import scalaz.Scalaz._

case class CookieOptions(
        domain  : String  = "",
        path    : String  = "",
        maxAge  : Int     = -1,
        secure  : Boolean = false,
        comment : String  = "",
        httpOnly: Boolean = false,
        encoding: String  = "UTF-8")


trait HttpCookie {
  implicit def cookieOptions: CookieOptions
  def name: String
  def value: String
  
  private def ensureDotDomain = if (!cookieOptions.domain.startsWith("."))
    "." + cookieOptions.domain
  else
    cookieOptions.domain
  
  def toCookieString = {
    val sb = new StringBuffer
    sb append name append "="
    sb append value

    if(cookieOptions.domain != null && cookieOptions.domain.trim().nonEmpty) 
      sb.append("; Domain=").append(ensureDotDomain.toLowerCase(Locale.ENGLISH))

    val pth = cookieOptions.path
    if(pth.isNonBlank) sb append "; Path=" append (if(!pth.startsWith("/")) {
      "/" + pth
    } else { pth })

    if(cookieOptions.comment.isNonBlank) sb append ("; Comment=") append cookieOptions.comment

    if(cookieOptions.maxAge > -1) sb append "; Max-Age=" append cookieOptions.maxAge

    if (cookieOptions.secure) sb append "; Secure"
    if (cookieOptions.httpOnly) sb append "; HttpOnly"
    sb.toString
  }
  
}

case class RequestCookie(name: String, value: String, cookieOptions: CookieOptions = CookieOptions()) extends HttpCookie
case class Cookie(name: String, value: String)(implicit cookieOptions: CookieOptions = CookieOptions()) extends HttpCookie

class CookieJar(private val reqCookies: Map[String, RequestCookie]) {
  private lazy val cookies = mutable.HashMap[String, HttpCookie]() ++ reqCookies

  def get(key: String) = cookies.get(key) map (_.value)

  def apply(key: String) = get(key) getOrElse (throw new Exception("No cookie could be found for the specified key [%s]" format key))

  def update(name: String, value: String)(implicit cookieOptions: CookieOptions=CookieOptions()) = {
    cookies += name -> value
    // TODO: actually add cookie to response
  }

  def set(name: String, value: String)(implicit cookieOptions: CookieOptions=CookieOptions()) = {
    this.update(name, value)(cookieOptions)
  }

  def delete(name: String)(implicit cookieOptions: CookieOptions = CookieOptions()) {
    cookies -= name
    // TODO: actually remove cookie from response
  }

  def +=(keyValuePair: (String, String))(implicit cookieOptions: CookieOptions = CookieOptions()) = {
    this.update(keyValuePair._1, keyValuePair._2)(cookieOptions)
  }

  def -=(key: String)(implicit cookieOptions: CookieOptions = CookieOptions()) {
    delete(key)(cookieOptions)
  }

}

