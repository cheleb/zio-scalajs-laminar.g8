package $package$.http

import zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.capabilities.zio.ZioStreams

import dev.cheleb.ziotapir.BaseController

import controllers.*

import $package$.service.*

//https://tapir.softwaremill.com/en/latest/server/logic.html
object HttpApi {
  private def gatherRoutes(
    controllers: List[dev.cheleb.ziotapir.BaseController]
  ): (List[ServerEndpoint[Any, Task]], List[ZServerEndpoint[Any, ZioStreams]]) =
    controllers.foldLeft((List.empty[ServerEndpoint[Any, Task]], List.empty[ZServerEndpoint[Any, ZioStreams]])) {
      case ((acc1, acc2), controller) =>
        val (routes1, routes2) = controller.routes
        (acc1 ++ routes1, acc2 ++ routes2)
    }

  private def makeControllers = for {
    healthController <- HealthController.makeZIO
    personController <- PersonController.makeZIO
  } yield List(healthController, personController)

  val endpointsZIO
    : URIO[PersonService & JWTService, (List[ServerEndpoint[Any, Task]], List[ZServerEndpoint[Any, ZioStreams]])] =
    makeControllers.map(gatherRoutes)
}
