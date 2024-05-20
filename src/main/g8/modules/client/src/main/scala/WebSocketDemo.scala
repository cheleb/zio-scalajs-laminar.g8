package samples

//import com.raquo.laminar.api.L.*
//import be.doeraene.webcomponents.ui5.*
//import be.doeraene.webcomponents.ui5.configkeys.*
//import io.laminext.websocket._
//import org.scalajs.dom.KeyCode

object WebSocketDemo {
/*
  private def sherpal =
    img(src := "images/avatars/ono.png", alt := "Ono")

  val ws =
    WebSocket
      .url("ws://localhost:8080/subscriptions")
      .string
      .build(managed = false)

  val nameVar = Var("")
  val inputElement = input(
    cls("new-todo"),
    placeholder("What needs to be done?"),
    autoFocus(true),
    inContext { thisNode =>
      // Note: mapTo below accepts parameter by-name, evaluating it on every enter key press
      onKeyPress
        .filter(_.keyCode == KeyCode.Enter)
        .mapTo(thisNode.ref.value)
        .filter(_.nonEmpty) --> { text =>
        thisNode.ref.value = "" // clear input
        ws.sendOne(text)
      }
    }
  )

  val wsPanel = div(
    span(
      Avatar(sherpal),
      Button("Connect!", _.events.onClick --> ws.reconnect)
    ),
    Panel(
      width := "50%",
      _.headerText := "Both expandable and expanded",
      children.command <-- ws.received.map { msg =>
        CollectionCommand.Append(
          div(Label(_.wrappingType := WrappingType.Normal, msg))
        )
      }
    ),
    span(
      inputElement,
      button(
        "Sendeee"
      )
    )
  )
*/
}
