package $package$.domain

import zio.json.JsonCodec
import sttp.tapir.Schema
import java.time.ZonedDateTime

case class User(
  name: String,
  age: Int,
  pet: Either[Cat, Dog],
  creationDate: ZonedDateTime
) derives JsonCodec,
      Schema
