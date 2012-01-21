package org.scalatra
package util

import org.mozilla.universalchardet.UniversalDetector
import java.io.{FileInputStream, File}
import scalaz._
import Scalaz._
import scala.io.Codec
import java.nio.charset.Charset

object FileCharset {

  def apply(file: File) = {
    val buf = Array.ofDim[Byte](4096)
    val detector = new UniversalDetector(null)
    try {
      val fis = new FileInputStream(file)
      try {
        var idx = fis.read(buf)
        while(idx > 0 && !detector.isDone) {
          detector.handleData(buf, 0, idx)
          idx = fis.read(buf)
        }
        detector.dataEnd()
      } finally {
        fis.close()
      }

      detector.getDetectedCharset.blankOption some Charset.forName none Codec.UTF8
    } finally {
      detector.reset()
    }
  }
}
