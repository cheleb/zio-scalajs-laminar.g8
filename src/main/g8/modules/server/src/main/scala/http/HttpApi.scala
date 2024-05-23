package samples

import zio.*
import sttp.tapir.server.ServerEndpoint

object HttpApi {
  def gatherRoutes(
      controllers: List[BaseController]
  ): List[ServerEndpoint[Any, Task]] =
    controllers.flatMap(_.routes)

  def makeControllers = for healthController <- HealthController.makeZIO
  yield List(healthController)

  val endpointsZIO = makeControllers.map(gatherRoutes)
}
