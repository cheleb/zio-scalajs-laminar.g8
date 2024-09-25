package $package$.domain

import zio.json.JsonCodec
import sttp.tapir.Schema
import java.time.ZonedDateTime

case class User(
  id: Long,
  name: String,
  email: String,
  petId: Option[Long],
  petType: Option[PetType],
  hashedPassword: String,
  age: Int,
  creationDate: ZonedDateTime
) derives JsonCodec,
      Schema

case class UserID(id: Long, email: String) derives JsonCodec, Schema
