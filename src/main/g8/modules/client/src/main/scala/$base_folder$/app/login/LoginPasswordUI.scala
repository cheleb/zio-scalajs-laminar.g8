package $package$.app.login

import dev.cheleb.scalamigen.NoPanel

import $package$.login.LoginPassword
import $package$.domain.Password

@NoPanel
final case class LoginPasswordUI(login: String, password: Password):
  def http: LoginPassword = LoginPassword(login, password)
