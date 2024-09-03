package $package$.app.login

import dev.cheleb.scalamigen.NoPanel

import $package$.login.LoginPassword

@NoPanel
final case class LoginPasswordUI(login: String, password: String):
  def http: LoginPassword = LoginPassword(login, password)
