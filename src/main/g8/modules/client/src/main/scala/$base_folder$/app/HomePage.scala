package $package$.app

import com.raquo.laminar.api.L.*

object HomePage:

  def apply(): HtmlElement =
    div(
      cls := "wrapper",
      div(
        cls := "scala",
        img(src := "img/scala.svg", width := "200px"),
        "Scala"
      ),
      div(
        cls := "jvm",
        img(src := "img/juke.png", width := "50px"),
        "Backend"
      ),
      div(
        cls := "js",
        img(src := "img/js.jpg", width := "150px"),
        "Frontend"
      ),
      div(
        display := "flex",
        cls     := "server",
        ul(
          li("zio-logging")
        )
      ),
      div(
        alignItems := "center",
        cls        := "shared",
        div(
          img(src := "img/zio.png"),
          ul(
            styleAttr := "float: right; clear: left; margin-right: 2em;",
            li("Iron")
          ),
          ul(
            li("zio-http"),
            li("zio-json"),
            li("Tapir", img(src := "img/tapir.svg", verticalAlign := "middle", width := "50px"))
          )
        )
      ),
      div(
        display := "flex",
        cls     := "client",
        img(marginRight := "1em", src := "img/laminar.png", width := "50px", height := "50px"),
        p(marginTop     := "1em", "Laminar")
      )
    )

  def demo(title: String, link: String) =
    li(
      a(
        href := link,
        title
      )
    )
