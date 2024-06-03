package $package$.core
import zio.json.*

import org.scalajs.dom

object Storage {

  def set[A: JsonCodec](key: String, value: A): Unit =
    dom.window.localStorage.setItem(key, value.toJson)

  def get[A: JsonCodec](key: String): Option[A] =
    Option(dom.window.localStorage.getItem(key))
      .filter(_.nonEmpty)
      .flatMap(_.fromJson[A].toOption)

  def remove(key: String): Unit =
    dom.window.localStorage.removeItem(key)

}
