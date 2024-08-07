import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val Versions = new {
    val chimney    = "1.3.0"
    val iron       = "2.6.0"
    val tapir      = "$tapir_version$"
    val sttp       = "3.9.6"
    val javaMail   = "1.6.2"
    val osLib      = "0.10.2"
    val scopt      = "4.1.0"
    val slf4j      = "2.0.13"
    val stripe     = "25.10.0"
    val flywaydb   = "10.14.0"
    val zio        = "2.1.2"
    val zioConfig  = "4.0.2"
    val zioJson    = "0.7.0"
    val zioLogging = "2.2.4"
    val zioPrelude = "1.0.0-RC27"
  }

  val serverLibraryDependencies =
    libraryDependencies ++= Seq(
      "io.github.iltotore"          %% "iron-zio-json"            % Versions.iron,
      "com.softwaremill.sttp.tapir" %% "tapir-zio"                % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"    % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server"   % Versions.tapir % Test
    )

  val sharedJvmAndJsLibraryDependencies =
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %%% "tapir-sttp-client" % Versions.tapir,
      "com.softwaremill.sttp.tapir"   %%% "tapir-json-zio"    % Versions.tapir,
      "com.softwaremill.sttp.client3" %%% "zio"               % Versions.sttp,
      "dev.zio"                       %%% "zio-json"          % Versions.zioJson,
      "dev.zio"                       %%% "zio-prelude"       % Versions.zioPrelude,
      "io.scalaland"                  %%% "chimney"           % Versions.chimney
    )

  val staticFilesGeneratorDependencies =
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt"        % Versions.scopt,
      "com.lihaoyi"      %% "os-lib"       % Versions.osLib,
      "org.slf4j"         % "slf4j-simple" % Versions.slf4j
    )
}
