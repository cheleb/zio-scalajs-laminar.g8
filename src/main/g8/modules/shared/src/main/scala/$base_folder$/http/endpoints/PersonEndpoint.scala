package $package$.http.endpoints

import zio.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import $package$.domain.*
import $package$.login.LoginPassword
import sttp.model.HeaderNames

object PersonEndpoint extends BaseEndpoint:

  val create: PublicEndpoint[Person, Throwable, User, Any] = baseEndpoint
    .tag("person")
    .name("person")
    .post
    .in("person")
    .in(
      jsonBody[Person]
        .description("Person to create")
        .example(
          Person(
            "John",
            "john.doe@foo.bar",
            Password("notsecured"),
            Password("notsecured"),
            42,
            Left(Cat("Fluffy"))
          )
        )
    )
    .out(jsonBody[User])
    .description("Create person")

  val login: PublicEndpoint[LoginPassword, Throwable, UserToken, Any] = baseEndpoint
    .tag("person")
    .name("login")
    .post
    .in("login")
    .in(
      jsonBody[LoginPassword]
    )
    .out(jsonBody[UserToken])
    .description("Login")

  val profile: Endpoint[String, Boolean, Throwable, (User, Option[Pet]), Any] = baseSecuredEndpoint
    .tag("person")
    .name("profile")
    .get
    .in("profile")
    .in(query[Boolean]("withPet").default(false))
    .out(jsonBody[(User, Option[Pet])])
    .description("Get profile")
