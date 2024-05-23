package $package$.http.controllers

import zio.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import $package$.http.endpoints.PersonEndpoint
import $package$.service.PersonService

class PersonController private (personService: PersonService)
    extends BaseController
    with PersonEndpoint {

  val create: ServerEndpoint[Any, Task] = createEndpoint
    .zServerLogic(p => personService.register(p))

  val routes: List[ServerEndpoint[Any, Task]] =
    List(create)
}

object PersonController {
  def makeZIO =
    ZIO.service[PersonService].map(new PersonController(_))
}
