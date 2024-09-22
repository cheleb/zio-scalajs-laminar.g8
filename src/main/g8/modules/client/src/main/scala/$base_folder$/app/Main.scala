package $package$.app

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import frontroute.LinkHandler

import dev.cheleb.ziolaminartapir.SameOriginBackendClientLive

@main def main: Unit =

  val myApp =
    div(
      onMountCallback(_ => session.loadUserState(SameOriginBackendClientLive.backendBaseURL)),
      Header(),
      Router(),
      Footer()
    ).amend(LinkHandler.bind) // For interbal links

  val containerNode = dom.document.getElementById("app")
  render(containerNode, myApp)
