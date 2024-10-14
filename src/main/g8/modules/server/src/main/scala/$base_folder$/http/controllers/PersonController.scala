package $package$.http.controllers

import dev.cheleb.ziojwt.SecuredBaseController

import zio.*

import sttp.model.Uri
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*

import $package$.domain.*
import $package$.http.endpoints.PersonEndpoint
import $package$.service.PersonService
import $package$.service.JWTService
import $package$.domain.errors.NotHostHeaderException

class PersonController private (personService: PersonService, jwtService: JWTService)
    extends BaseController
    with SecuredBaseController[String, UserID](jwtService.verifyToken) {

  val create: ServerEndpoint[Any, Task] = PersonEndpoint.create
    .zServerLogic:
      personService.register

  val login: ServerEndpoint[Any, Task] = PersonEndpoint.login.zServerLogic { lp =>
    for {
      user  <- personService.login(lp.login, lp.password)
      token <- jwtService.createToken(user)
    } yield token
  }

  val profile: ServerEndpoint[Any, Task] = PersonEndpoint.profile.securedServerLogic { userId => withPet =>
    personService.getProfile(userId, withPet)
  }
  
  val routes: List[ServerEndpoint[Any, Task]] =
    List(create, login, profile)
}

object PersonController {
  def makeZIO: URIO[PersonService & JWTService, PersonController] =
    for
      jwtService    <- ZIO.service[JWTService]
      personService <- ZIO.service[PersonService]
    yield new PersonController(personService, jwtService)
}
