package $package$.domain

import zio.json.JsonCodec
import sttp.tapir.Schema
import zio.prelude.*
import zio.prelude.Debug.Repr
import zio.prelude.Debug.Renderer

import zio.prelude.derivation.DebugGen

case class Person(
  name: String,
  email: String,
  password: Password,
  passwordConfirmation: Password,
  age: Int,
  pet: Either[Cat, Dog]
) derives JsonCodec,
      Schema

object Person:
  given Debug[Person] = DebugGen.derived[Person]

opaque type Password = String

object Password:
  given JsonCodec[Password] = JsonCodec.string
  given Schema[Password]    = Schema.string

  given Debug[Password] with
    def debug(value: Password): Repr = Repr.String("*****")

  def apply(password: String): Password = password
