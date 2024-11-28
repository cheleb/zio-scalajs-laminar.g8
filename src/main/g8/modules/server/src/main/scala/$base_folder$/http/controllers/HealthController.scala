package $package$.http.controllers

import zio.*
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*

import $package$.http.endpoints.HealthEndpoint

class HealthController private
    extends dev.cheleb.ziotapir.BaseController {

  val health = HealthEndpoint.healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("OK"))
  override val routes: (List[ServerEndpoint[Any, Task]], List[ZServerEndpoint[Any, ZioStreams]]) =
    (List(health), List.empty)
}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
