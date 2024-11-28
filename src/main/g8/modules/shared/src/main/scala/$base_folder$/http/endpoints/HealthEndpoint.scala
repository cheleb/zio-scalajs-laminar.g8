package $package$.http.endpoints

import sttp.tapir.*
import zio.*

object HealthEndpoint {
  val healthEndpoint = endpoint
    .tag("health")
    .name("health")
    .get
    .in("health")
    .out(stringBody)
    .description("Health check")

}
