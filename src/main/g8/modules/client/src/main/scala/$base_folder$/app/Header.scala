package $package$.app

import com.raquo.laminar.api.L.*

object Header:
  def apply(): HtmlElement =
    div(
      h1("Scala3 Laminar Frontroute Header")
    )
