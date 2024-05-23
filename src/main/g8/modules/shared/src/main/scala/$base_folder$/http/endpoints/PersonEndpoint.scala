package $package$.http.endpoints

import sttp.tapir.*
import zio.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import $package$.domain.*

trait PersonEndpoint extends BaseEndpoint {
  val createEndpoint = baseEndpoint
    .tag("person")
    .name("person")
    .post
    .in("person")
    .in(jsonBody[Person])
    .out(jsonBody[Person])
    .description("Create person")

}
