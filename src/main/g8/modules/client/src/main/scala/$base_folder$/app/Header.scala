package $package$.app

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.*

import dev.cheleb.scalamigen.{*, given}
import dev.cheleb.ziolaminartapir.ZJS.*

import $package$.app.login.LoginPasswordUI
import $package$.http.endpoints.PersonEndpoint
import $package$.domain.UserToken
import dev.cheleb.ziolaminartapir.Session

object Header:
  private val openPopoverBus = new EventBus[Boolean]
  private val profileId      = "profileId"

  val credentials = Var(LoginPasswordUI("", ""))

  def apply(): HtmlElement =
    div(
      ShellBar(
        _.primaryTitle       := "ZIO Laminar Demo",
        _.secondaryTitle     := "Secondary title",
        _.notificationsCount := "99+",
        _.showNotifications  := true,
//        _.showProductSwitch  := true,
        _.showCoPilot   := true,
        _.slots.profile := Avatar(idAttr := profileId, img(src := "questionmark.jpg")),
        _.events.onProfileClick.mapTo(true) --> openPopoverBus.writer
      ),
      Popover(
        _.openerId := profileId,
        _.open <-- openPopoverBus.events,
        // _.placement := PopoverPlacementType.Bottom,
        div(Title(padding := "0.25rem 1rem 0rem 1rem", "Login")),
        div(
          child.maybe <--
            session(
              UList(
                _.separators := ListSeparator.None,
                _.item(_.icon := IconName.settings, a("Settings", href := "/profile")),
                _.item(_.icon := IconName.`sys-help`, "Help"),
                _.item(_.icon := IconName.log, "Sign out").amend(
                  onClick --> { _ =>
                    session.clearUserState()
                    openPopoverBus.emit(false)
                  }
                )
              )
            )(
              div(
                credentials.asForm,
                Button(
                  "Login",
                  onClick --> { _ =>
                    loginHandler(session)
                    openPopoverBus.emit(false)
                  }
                )
              )
            )
        )
      )
    )

  def loginHandler(session: Session[UserToken]): Unit =
    PersonEndpoint
      .login(credentials.now().http)
      .map(token => session.setUserState(token))
      .runJs
