package $package$.http

import zio.*
import zio.http.*

import sttp.tapir.*
import sttp.tapir.files.*
import sttp.tapir.server.ziohttp.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.interceptor.cors.CORSInterceptor

import $package$.service.*
import $package$.http.prometheus.*
$if(db.truthy)$
import $package$.services.*
import $package$.repositories.*
$endif$

object HttpServer extends ZIOAppDefault {

  private val webJarRoutes = staticResourcesGetServerEndpoint[Task]("public")(
    this.getClass.getClassLoader,
    "public"
  )

  val serverOptions: ZioHttpServerOptions[Any] =
    ZioHttpServerOptions.customiseInterceptors
      .metricsInterceptor(metricsInterceptor)
      .appendInterceptor(
        CORSInterceptor.default
      )
      .options

  $if(db.truthy)$
  val runMigrations = for {
    flyway <- ZIO.service[FlywayService]
    _ <- flyway.runMigrations().catchSome { case e =>
           ZIO.logError(s"Error running migrations: \${e.getMessage()}")
             *> flyway.runRepair() *> flyway.runMigrations()
         }
  } yield ()
  $endif$

  private val $if(db.truthy)$server$else$serverProgram$endif$ =
    for {
      _            <- Console.printLine("Starting server...")
      apiEndpoints <- HttpApi.endpointsZIO
      docEndpoints = SwaggerInterpreter()
                       .fromServerEndpoints(apiEndpoints, "zio-laminar-demo", "1.0.0")
      _ <- Server.serve(
             Routes(
               Method.GET / Root -> handler(Response.redirect(url"public/index.html"))
             ) ++
               ZioHttpInterpreter(serverOptions)
                 .toHttp(metricsEndpoint :: webJarRoutes :: apiEndpoints ::: docEndpoints)
           )
    } yield ()

  $if(db.truthy)$
  private val program =
    for {
      _ <- runMigrations
      _ <- server
    } yield ()
  $endif$

  override def run =
    $if(db.truthy)$program$else$serverProgram$endif$
      .provide(
        Server.default,
        // Service layers
        $if(db.truthy)$
        PersonServiceLive.layer,
        FlywayServiceLive.configuredLayer,
        JWTServiceLive.configuredLayer,
        // Repository layers
        UserRepositoryLive.layer,
        PetRepositoryLive.layer,
        Repository.dataLayer
        $endif$
      )
}
