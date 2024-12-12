package $package$.http

import zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.capabilities.zio.ZioStreams

import dev.cheleb.ziotapir.*

import controllers.*

import $package$.service.*

object HttpApi extends Routes {

  private def makeControllers = for {
    healthController <- HealthController.makeZIO
    personController <- PersonController.makeZIO
  } yield List(healthController, personController)

  def endpointsZIO: URIO[PersonService & JWTService, List[ServerEndpoint[Any, Task]]] =
    makeControllers.map(gatherRoutes(_.routes))

  def streamEndpointsZIO: URIO[PersonService & JWTService, List[ServerEndpoint[ZioStreams, Task]]] =
    makeControllers.map(gatherRoutes(_.streamRoutes))

  def endpoints = for {
    endpoints       <- endpointsZIO
    streamEndpoints <- streamEndpointsZIO
  } yield endpoints ++ streamEndpoints
}
