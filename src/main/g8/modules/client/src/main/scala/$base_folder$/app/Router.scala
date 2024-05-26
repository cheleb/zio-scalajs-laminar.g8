package $package$.app

import com.raquo.laminar.api.L.*
import frontroute.*

import $package$.app.demos.*

object Router:
  val externalUrlBus = EventBus[String]()
  def apply() =
    mainTag(
      //onMountCallback(ctx => externalUrlBus.events.foreach(url => dom.window.location.href = url)(ctx.owner)),
      routes(
        div(
          cls := "container-fluid",
          // potentially children
          (pathEnd | path("demos")) {
            DemosPage()
          },
          // path("login") {
          //   LoginPage()
          // },
          // path("signup") {
          //   SignUpPage()
          // },
          // path("change-password") {
          //   ChangePasswordPage()
          // },
          // path("forgot-password") {
          //   ForgotPasswordPage()
          // },
          // path("recover-password") {
          //   RecoverPasswordPage()
          // },
          // path("logout") {
          //   LogoutPage()
          // },
          // path("profile") {
          //   ProfilePage()
          // },
          // path("post") {
          //   CreateCompanyPage()
          // },
          // path("company" / long) {
          //   companyId =>
          //     CompanyPage(companyId)
          // },
          // noneMatched {
          //   NotFoundPage()
          // }
        )
      )
    )
