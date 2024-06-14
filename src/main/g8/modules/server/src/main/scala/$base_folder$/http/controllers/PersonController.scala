package $package$.http.controllers

import zio.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import $package$.http.endpoints.PersonEndpoint
import $package$.service.PersonService

class PersonController private (personService: PersonService) extends BaseController {

  val create: ServerEndpoint[Any, Task] = PersonEndpoint.createEndpoint
    .zServerLogic(p => personService.register(p))

  val routes: List[ServerEndpoint[Any, Task]] =
    List(create)
}

object PersonController {
  def makeZIO: URIO[PersonService, PersonController] =
    ZIO.service[PersonService].map(new PersonController(_))
}
