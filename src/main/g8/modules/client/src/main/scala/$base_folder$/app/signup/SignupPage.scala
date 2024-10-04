package $package$.app.signup

import zio.prelude.*

import be.doeraene.webcomponents.ui5.Button

import com.raquo.laminar.api.L.*

import $package$.app.given
import $package$.domain.*

import dev.cheleb.scalamigen.*

import dev.cheleb.ziolaminartapir.*

import $package$.http.endpoints.PersonEndpoint

object SignupPage:
  def apply() =
    val personVar = Var(
      Person("John", "john.does@foo.bar", Password("notsecured"), Password("notsecured"), 42, Left(Cat("Fluffy")))
    )
    val userBus  = EventBus[User]()
    val errorBus = EventBus[Throwable]()

    div(
      styleAttr := "width: 100%; overflow: hidden;",
      h1("Signup"),
      div(
        styleAttr := "width: 600px; float: left;",
        personVar.asForm,
        child.maybe <-- personVar.signal.map {
          case Person(_, _, password, passwordConfirmation, _, _) if password != passwordConfirmation =>
            Some(div("Passwords do not match"))
          case _ => None
        }
      ),
      div(
        styleAttr := "width: 100px; float: left; margin-top: 200px;",
        Button(
          "-- Post -->",
          onClick --> { _ =>
            // scalafmt:off

            PersonEndpoint
              .createEndpoint(personVar.now())
              .emitTo(userBus, errorBus)

            // scalafmt:on

          }
        )
      ),
      div(
        styleAttr := "width: 600px; float: left;",
        h1("Databinding"),
        child.text <-- personVar.signal.map(p => s"\${p.render}"),
        h1("Response"),
        child <-- userBus.events.map(renderUser),
        h1("Errors"),
        child <-- errorBus.events.map(e => div(s"Error: \${e.getMessage}"))
      )
    )

  def renderUser(user: User) =
    div(
      h2("User"),
      div(s"Id: \${user.id}"),
      div(s"Name: \${user.name}"),
      div(s"Age: \${user.age}"),
      div(s"Creation Date: \${user.creationDate}")
    )
