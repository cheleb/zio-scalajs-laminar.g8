import sbt._

object Dependencies {
  val Versions = new {
    val zio        = "2.1.2"
    val tapir      = "$tapir_version$"
    val zioLogging = "2.2.4"
    val zioConfig  = "4.0.2"
    val sttp       = "3.9.6"
    val javaMail   = "1.6.2"
    val stripe     = "25.10.0"
    val flywaydb   = "10.14.0"
  }
}
