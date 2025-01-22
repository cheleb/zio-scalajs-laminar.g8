package $package$.http.controllers

import zio.*
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint

import $package$.http.endpoints.HealthEndpoint
import dev.cheleb.ziotapir.BaseController

class HealthController private
    extends BaseController {

  val health = HealthEndpoint.healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("OK"))
  override val routes: List[ServerEndpoint[Any, Task]] = List(health)
}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
