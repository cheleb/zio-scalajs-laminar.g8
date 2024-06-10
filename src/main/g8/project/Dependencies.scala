import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

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

  val serverLibraryDependencies =
    libraryDependencies ++= Seq(
      "io.github.iltotore"          %% "iron-zio-json"            % "2.5.0",
      "com.softwaremill.sttp.tapir" %% "tapir-zio"                % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"    % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server"   % Versions.tapir % "test"
    )

  val sharedJvmAndJsLibraryDependencies =
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %%% "tapir-sttp-client" % Versions.tapir,
      "com.softwaremill.sttp.tapir"   %%% "tapir-json-zio"    % Versions.tapir,
      "com.softwaremill.sttp.client3" %%% "zio"               % Versions.sttp,
      "dev.zio"                       %%% "zio-json"          % "0.6.2",
      "dev.zio"                       %%% "zio-prelude"       % "1.0.0-RC26",
      "io.scalaland"                  %%% "chimney"           % "1.1.0"
    )

  val staticFilesGeneratorDependencies =
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt"        % "4.1.0",
      "com.lihaoyi"      %% "os-lib"       % "0.10.1",
      "org.slf4j"         % "slf4j-simple" % "2.0.13"
    )
}
