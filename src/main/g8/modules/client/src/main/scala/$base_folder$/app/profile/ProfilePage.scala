package $package$.app.profile

import com.raquo.laminar.api.L.*

import $package$.app.given
import $package$.domain.*
import $package$.http.endpoints.PersonEndpoint

import dev.cheleb.ziolaminartapir.*

object ProfilePage:

  val userBus = new EventBus[User]

  def apply() = div(
    child <-- session(div(h1("Please log in to view your profile")))(_ =>
      div(
        onMountCallback { _ =>
          PersonEndpoint.profile(()).emitTo(userBus)
        },
        h1("Profile Page"),
        child <-- userBus.events.map { user =>
          div(
            h2("User"),
            div("Name: ", user.name),
            div("Email: ", user.email),
            div("Age: ", user.age.toString)
          )
        }
      )
    )
  )
  