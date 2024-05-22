package endpoints

import sttp.tapir.*
import zio.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import domain.*

trait PersonEndpoint {
  val personEndpoint = endpoint
    .tag("person")
    .name("person")
    .post
    .in("person")
    .out(jsonBody[Person])
    .description("Create person")

}
