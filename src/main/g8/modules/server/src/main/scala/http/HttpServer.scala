package http

import zio.*
import zio.http.*
import sttp.tapir.files.*
import sttp.tapir.*
import sttp.tapir.server.ziohttp.*

object HttpServer extends ZIOAppDefault {

  val webJarRoutes = staticResourcesGetServerEndpoint[Task]("public")(
    this.getClass.getClassLoader,
    "public"
  )

  val serrverProgram =
    for _ <- ZIO.succeed(println("Hello world"))
    endpoints <- HttpApi.endpointsZIO
    _ <- Server.serve(
      ZioHttpInterpreter(ZioHttpServerOptions.default)
        .toHttp(webJarRoutes :: endpoints)
    )
    yield ()

  override def run =
    serrverProgram
      .provide(
        Server.default
      )
}
