package $package$.app

import com.raquo.laminar.api.L.*
import org.scalajs.dom

@main def main: Unit =

  val myApp =
    div(
      Header(),
      Router(),
      Footer()
    )

  val containerNode = dom.document.getElementById("app")
  render(containerNode, myApp)
