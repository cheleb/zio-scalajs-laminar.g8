package $package$.http

import zio.*
import sttp.tapir.server.ServerEndpoint

import controllers.*

//https://tapir.softwaremill.com/en/latest/server/logic.html
object HttpApi {
  def gatherRoutes(
      controllers: List[BaseController]
  ): List[ServerEndpoint[Any, Task]] =
    controllers.flatMap(_.routes)

  def makeControllers = for healthController <- HealthController.makeZIO
  yield List(healthController)

  val endpointsZIO = makeControllers.map(gatherRoutes)
}
