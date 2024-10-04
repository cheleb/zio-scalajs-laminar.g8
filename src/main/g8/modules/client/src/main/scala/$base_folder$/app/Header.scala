package $package$.app

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.*

import dev.cheleb.scalamigen.*
import dev.cheleb.ziolaminartapir.*

import $package$.login.LoginPassword
import $package$.http.endpoints.PersonEndpoint
import $package$.domain.UserToken
import dev.cheleb.ziolaminartapir.Session
import $package$.domain.Password

object Header:
  private val openPopoverBus = new EventBus[Boolean]
  private val profileId      = "profileId"

  private val loginErrorEventBus   = new EventBus[Throwable]
  private val loginSuccessEventBus = new EventBus[Unit]

  val credentials = Var(LoginPassword("", Password("")))

  given Form[Password] = secretForm(Password(_))

  def apply(): HtmlElement =
    div(
      ShellBar(
        _.slots.startButton  := a(Icon(_.name := IconName.home, cls := "pad-10"), href := "/"),
        _.primaryTitle       := "ZIO Laminar Demo",
        _.secondaryTitle     := "And Tapir, UI5, and more",
        _.notificationsCount := "99+",
        _.showNotifications  := true,
        _.showCoPilot        := true,
        _.slots.profile      := Avatar(idAttr := profileId, img(src := "img/questionmark.jpg")),
        _.events.onProfileClick.mapTo(true) --> openPopoverBus.writer
      ),
      Popover(
        _.openerId := profileId,
        _.open <-- openPopoverBus.events.mergeWith(loginSuccessEventBus.events.map(_ => false)),
        // _.placement := PopoverPlacementType.Bottom,
        div(Title(padding := "0.25rem 1rem 0rem 1rem", "Sign in / up")),
        child <-- session(notLogged)(logged)
      )
    )

  def notLogged =
    div(
      credentials.asForm,
      child <-- loginErrorEventBus.events.map { error =>
        div(
          cls := "center",
          Text(s"Error: ${error.getMessage}")
        )
      },
      div(
        cls := "center",
        Button(
          "Login",
          disabled <-- credentials.signal.map(_.isIncomplete),
          onClick --> { _ =>
            loginHandler(session)
          }
        )
      ),
      a("Sign up", href := "/signup")
        .amend(
          onClick --> { _ =>
            openPopoverBus.emit(false)
          }
        )
    )

  def logged(userToken: UserToken) =
    UList(
      _.separators := ListSeparator.None,
      _.item(
        _.icon             := IconName.settings,
        a("Settings", href := "/profile", title := s" Logged in as ${userToken.email}")
      )
        .amend(
          onClick --> { _ =>
            openPopoverBus.emit(false)
          }
        ),
      _.item(_.icon := IconName.`sys-help`, "Help"),
      _.item(_.icon := IconName.log, "Sign out").amend(
        onClick --> { _ =>
          session.clearUserState()
          openPopoverBus.emit(false)
        }
      )
    )

  def loginHandler(session: Session[UserToken]): Unit =
    PersonEndpoint
      .login(credentials.now())
      .map(token => session.saveToken(SameOriginBackendClientLive.backendBaseURL, token))
      .emitTo(loginSuccessEventBus, loginErrorEventBus)
