package $package$.domain

import sttp.tapir.Schema
import zio.json.JsonCodec
import zio.prelude.*
import zio.prelude.Debug.Repr
import zio.prelude.magnolia.*

import dev.cheleb.scalamigen.NoPanel

@NoPanel
case class Person(
  name: String,
  email: String,
  password: Password,
  passwordConfirmation: Password,
  age: Int,
  pet: Either[Cat, Dog]
) derives JsonCodec,
      Schema,
      Debug {
  def errorMessages = {
    val passwordErrors = if password == passwordConfirmation then Nil else List("Passwords do not match")
    val ageErrors      = if age >= 18 then Nil else List("You must be at least 18 years old")
    passwordErrors ++ ageErrors
  }
}

opaque type Password <: String = String

object Password:
  given JsonCodec[Password] = JsonCodec.string
  given Schema[Password]    = Schema.string

  given Debug[Password] with
    def debug(value: Password): Repr = Repr.String("*****")

  def apply(password: String): Password = password
