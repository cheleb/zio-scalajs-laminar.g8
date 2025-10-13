package $package$.domain

import zio.json.JsonCodec
import sttp.tapir.Schema
import zio.prelude.Debug
import zio.prelude.magnolia.*
import sttp.tapir.CodecFormat
import sttp.tapir.Codec
import zio.json.jsonDiscriminator

@jsonDiscriminator("type")
sealed trait Pet derives JsonCodec, Schema, Debug {
  def name: String
}

case class Cat(name: String)           extends Pet derives JsonCodec, Schema, Debug
case class Dog(name: String, age: Int) extends Pet derives JsonCodec, Schema, Debug

enum PetType derives JsonCodec, Schema, Debug:
  case Cat, Dog

object PetType:
  given Codec[String, PetType, CodecFormat.TextPlain] = Codec.string.map(PetType.valueOf)(_.toString)
