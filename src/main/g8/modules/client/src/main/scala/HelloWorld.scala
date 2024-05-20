package samples

import org.scalajs.dom
import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*

case class Sample(name: String, component: HtmlElement)

object App extends App {

  val sample = Var(samples.tree.component)

  private def item(name: String) = SideNavigation.item(
    _.text := name,
    dataAttr("component-name") := name
  )

  private val demos = Seq(
    samples.simple,
    samples.either,
    samples.enums,
    samples.person,
    samples.validation,
    samples.list,
    samples.tree
  )

  val myApp =
    div(
      display := "flex",
      div(
        paddingRight("2rem"),
        Title(
          "Demos",
          padding("0.5rem"),
          cursor := "pointer"
        ),
        SideNavigation(
          _.events.onSelectionChange
            .map(_.detail.item.dataset.get("componentName")) --> Observer[
            Option[String]
          ] { name =>
            val el = name
              .flatMap(n => demos.find(_.name == n))

            sample.set(el.map(_.component).getOrElse(div("Not found!")))

          },
          demos.map(_.name).map(item)
        )
      ),
      div(
        height := "100vh",
        overflowY := "auto",
        display := "flex",
        flexGrow := 1,
        div(
          padding := "10px",
          minWidth := "40%",
          maxWidth := "calc(100% - 320px)",
          child <-- sample.signal
        )
      )
    )

  val containerNode = dom.document.getElementById("app")
  render(containerNode, myApp)
}
