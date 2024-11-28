package $package$.app

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import com.raquo.laminar.api.L.*

import dev.cheleb.scalamigen.*
import dev.cheleb.ziotapir.laminar.*

import $package$.login.LoginPassword
import $package$.http.endpoints.PersonEndpoint
import $package$.domain.UserToken
import $package$.domain.Password

import scala.concurrent.duration.DurationInt

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
        _.slots.startButton := a(
          Icon(_.name := IconName.home, cls := "pad-10"),
          href := Router.uiRoute()
        ),
        _.primaryTitle       := "$name$",
        _.secondaryTitle     := "$short-description$",
        _.notificationsCount := "2+",
        _.showNotifications  := true,
        _.showCoPilot        := true,
        _.slots.profile      := Avatar(idAttr := profileId, img(src := "img/questionmark.jpg")),
        _.events.onProfileClick.mapTo(true) --> openPopoverBus
      ),
      Popover(
        _.openerId := profileId,
        _.open <-- openPopoverBus.events
          .mergeWith(loginSuccessEventBus.events.mapTo(false)),
        div(
          Title("Sign in"),
          child <-- session(notLogged)(logged)
        )
      )
    )

  def notLogged =
    div(
      styleAttr := "padding: 1em;",
      credentials.asForm,
      Toast(
        cls := "srf-invalid",
        _.duration  := 2.seconds,
        _.placement := ToastPlacement.MiddleCenter,
        child <-- loginErrorEventBus.events.map(_.getMessage()),
        _.open <-- loginErrorEventBus.events.mapTo(true)
      ),
      div(
        cls := "center",
        Button(
          "Login",
          disabled <-- credentials.signal.map(_.isIncomplete),
          onClick --> loginHandler(session)
        )
      ),
      a("Sign up", href := Router.uiRoute("signup"))
        .amend(
          onClick.mapTo(false) --> openPopoverBus
        )
    )

  def logged(userToken: UserToken) =
    UList(
      _.separators := ListSeparator.None,
      _.item(
        _.icon             := IconName.settings,
        a("Settings", href := Router.uiRoute("profile"), title := s" Logged in as \${userToken.email}")
      )
        .amend(
          onClick.mapTo(false) --> openPopoverBus
        ),
 $if(scalablytyped.truthy)$
      _.item(_.icon := IconName.`bar-chart`, a("Statistics", href := Router.uiRoute("demos/scalablytyped"))),
 $endif$
      _.item(_.icon := IconName.log, "Sign out").amend(
        onClick --> { _ =>
          session.clearUserState()
          openPopoverBus.emit(false)
        }
      )
    )

  def loginHandler(session: Session[UserToken]): Observer[Any] = Observer[Any] { _ =>
    PersonEndpoint
      .login(credentials.now())
      .map(token => session.saveToken(token))
      .emitTo(loginSuccessEventBus, loginErrorEventBus)
  }
