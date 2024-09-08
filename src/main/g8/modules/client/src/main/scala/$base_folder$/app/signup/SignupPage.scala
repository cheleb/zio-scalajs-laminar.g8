package $package$.app.signup

import be.doeraene.webcomponents.ui5.Button

import com.raquo.laminar.api.L.*

import com.example.ziolaminardemo.app.given

import dev.cheleb.scalamigen.{*, given}

import $package$.domain.*
import dev.cheleb.ziolaminartapir.*
import dev.cheleb.ziolaminartapir.ZJS.*

import zio.prelude.*

import $package$.http.endpoints.PersonEndpoint

given Form[Password] = secretForm(Password(_))

given Defaultable[Cat] with
  def default = Cat("")

given Defaultable[Dog] with
  def default = Dog("", 1)

object SignupPage:
  def apply() =
    val personVar = Var(
      Person("John", "john.does@foo.bar", Password("notsecured"), Password("notsecured"), 42, Left(Cat("Fluffy")))
    )
    val userBus = EventBus[User]()

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
              .emitTo(userBus)

            // scalafmt:on

          }
        )
      ),
      div(
        styleAttr := "width: 600px; float: left;",
        h1("Databinding"),
        child.text <-- personVar.signal.map(p => s"\${p.render}"),
        h1("Response"),
        child <-- userBus.events.map(renderUser)
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
