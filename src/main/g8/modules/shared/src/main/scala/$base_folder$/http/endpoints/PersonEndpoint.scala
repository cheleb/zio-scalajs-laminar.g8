package $package$.http.endpoints

import sttp.tapir.*
import zio.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import $package$.domain.*

object PersonEndpoint extends BaseEndpoint:

  val createEndpoint: Endpoint[Unit, Person, Throwable, User, Any] = baseEndpoint
    .tag("person")
    .name("person")
    .post
    .in("person")
    .in(
      jsonBody[Person]
        .description("Person to create")
        .example(Person("John", 30, Left(Cat("Fluffy"))))
    )
    .out(jsonBody[User])
    .description("Create person")
