package $package$.login

import sttp.tapir.Schema
import dev.cheleb.scalamigen.NoPanel
import $package$.domain.Password

@NoPanel
final case class LoginPassword(login: String, password: Password) derives zio.json.JsonCodec, Schema:
  def isIncomplete: Boolean = login.isBlank || password.isBlank
