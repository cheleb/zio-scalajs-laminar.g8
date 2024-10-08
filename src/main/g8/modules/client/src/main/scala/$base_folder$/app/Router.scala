package $package$.app

import com.raquo.laminar.api.L.*
import frontroute.*

import org.scalajs.dom

import $package$.app.demos.*

object Router:
  private val externalUrlBus = EventBus[String]()
  val writer                 = externalUrlBus.writer
  def apply() =
    mainTag(
      linkHandler,
      routes(
        div(
          cls := "container-fluid",
          // potentially children
          (pathEnd | path("public") | path("public" / "index.html")) {
            HomePage()
          },
          $if(scalablytyped.truthy)$
          path("demos" / "scalablytyped") {
            scalablytyped.ScalablytypedDemoPage()
          },
          $endif$
          path("signup") {
            signup.SignupPage()
          },
          path("profile") {
            profile.ProfilePage()
          },
          noneMatched {
            div("404 Not Found")
          }
        )
      )
    )
  def linkHandler =
    onMountCallback(ctx => externalUrlBus.events.foreach(url => dom.window.location.href = url)(ctx.owner))
