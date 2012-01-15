package org.scalatra

import java.io.{InputStream, File}
import java.nio.charset.Charset


trait HttpFile {

  def name: String
  def contentType: String
  def size: Long

  def inputStream: InputStream
  def bytes: Array[Byte]
  def string: String

  def saveTo(file: File)
  def delete()
}