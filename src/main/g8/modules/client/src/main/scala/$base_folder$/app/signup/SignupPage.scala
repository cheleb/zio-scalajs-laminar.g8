package $package$.app.signup

import zio.prelude.*

import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.ToastPlacement

import com.raquo.laminar.api.L.*

import dev.cheleb.scalamigen.*
import dev.cheleb.ziotapir.laminar.*

import scala.concurrent.duration.DurationInt

import $package$.app.given
import $package$.domain.*
import $package$.http.endpoints.PersonEndpoint

object SignupPage:
  def apply() =
    val personVar = Var(
      Person("John", "john.does@foo.bar", Password("notsecured"), Password("notsecured"), 42, Left(Cat("Fluffy")))
    )
    val userBus  = EventBus[User]()
    val errorBus = EventBus[Throwable]()

    val debugVar = Var(false)

    div(
      h1("Signup"),
      div(
        styleAttr := "float: left;",
        //
        // The form is generated from the case class
        //
        personVar.asForm,
        children <-- personVar.signal.map {
          _.errorMessages.map(div(_)).toSeq
        }
      ),
      debugUI(debugVar, personVar),
      div(
        styleAttr := "max-width: fit-content; margin:1em auto",
        Button(
          "Create",
          disabled <-- personVar.signal.map(_.errorMessages.nonEmpty),
          onClick --> { _ =>
            // scalafmt:off

            PersonEndpoint
              .create(personVar.now())
              .emitTo(userBus, errorBus)

            // scalafmt:on

          }
        )
      ),
      renderToast(userBus, errorBus)
    )

  def renderUser(user: User) =
    div(
      h2("User"),
      div(s"Id: \${user.id}"),
      div(s"Name: \${user.name}"),
      div(s"Age: \${user.age}"),
      div(s"Creation Date: \${user.creationDate}")
    )

  def debugUI(debugVar: Var[Boolean], personVar: Var[Person]) =
    div(
      styleAttr := "float: right;",
      Switch(
        _.textOn  := "🔎",
        _.textOff := "🔎",
        _.tooltip := "On/Off Switch",
        onChange.mapToChecked --> debugVar
      ),
      div(
        styleAttr := "float: both;",
        child <-- debugVar.signal.map:
          case true =>
            div(
              styleAttr := "max-width: 300px; margin:1em auto",
              Title("Databinding"),
              child.text <-- personVar.signal.map(_.render)
            )
          case false => div()
      )
    )

  def renderToast(userBus: EventBus[User], errorBus: EventBus[Throwable]) =
    Seq(
      Toast(
        cls := "srf-valid",
        _.duration  := 2.seconds,
        _.placement := ToastPlacement.MiddleCenter,
        child <-- userBus.events.map(renderUser),
        _.open <-- userBus.events.map(_ => true)
      ),
      Toast(
        cls := "srf-invalid",
        _.duration  := 2.seconds,
        _.placement := ToastPlacement.MiddleCenter,
        child <-- errorBus.events.map(_.getMessage()),
        _.open <-- errorBus.events.map(_ => true)
      )
    )
