import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val Versions = new {
    val chimney               = "1.4.0"
    val flywaydb              = "10.19.0"
    val iron                  = "2.6.0"
    val javaMail              = "1.6.2"
    val laminarFormDerivation = "0.15.0"
    val osLib                 = "0.10.2"
    val postgresql            = "42.7.4"
    val quill                 = "4.8.5"
    val scopt                 = "4.1.0"
    val slf4j                 = "2.0.16"
    val stripe                = "25.10.0"
    val sttp                  = "3.9.6"
    val tapir                 = "1.11.5"
    val zio                   = "2.1.9"
    val zioConfig             = "4.0.2"
    val zioJson               = "0.7.3"
    val zioLaminarTapir       = "0.1.1"
    val zioLogging            = "2.2.4"
    val zioPrelude            = "1.0.0-RC31"
  }

  private val configDependencies = Seq(
    "dev.zio" %% "zio-config"          % Versions.zioConfig,
    "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig,
    "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig
  )

$if(db.truthy)$
  private val databaseDependencies = Seq(
    "org.flywaydb"   % "flyway-core"                % Versions.flywaydb,
    "org.flywaydb"   % "flyway-database-postgresql" % Versions.flywaydb,
    "org.postgresql" % "postgresql"                 % Versions.postgresql % Runtime
  )
$endif$
$if(quill.truthy)$
  private val quillDependencies = Seq(
    "io.getquill" %% "quill-jdbc-zio" % Versions.quill
  )
$endif$

  private val jwtDependencies = Seq(
    "com.auth0" % "java-jwt" % "4.4.0"
  )

  val serverLibraryDependencies =
    libraryDependencies ++= Seq(
      "io.github.iltotore"          %% "iron-zio-json"            % Versions.iron,
      "com.softwaremill.sttp.tapir" %% "tapir-zio"                % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"    % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % Versions.tapir,
      "dev.cheleb"                  %% "zio-jwt-server"           % Versions.zioLaminarTapir,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server"   % Versions.tapir % Test
    ) ++
      configDependencies$if(db.truthy)$ ++
      databaseDependencies$endif$$if(quill.truthy)$ ++
      quillDependencies$endif$ ++
      jwtDependencies

  val sharedJvmAndJsLibraryDependencies =
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %%% "tapir-sttp-client"              % Versions.tapir,
      "com.softwaremill.sttp.tapir"   %%% "tapir-json-zio"                 % Versions.tapir,
      "com.softwaremill.sttp.client3" %%% "zio"                            % Versions.sttp,
      "dev.cheleb"                    %%% "laminar-form-derivation-shared" % Versions.laminarFormDerivation,
      "dev.zio"                       %%% "zio-json"                       % Versions.zioJson,
      "dev.zio"                       %%% "zio-prelude"                    % Versions.zioPrelude,
      "dev.zio"                       %%% "zio-prelude-magnolia"           % Versions.zioPrelude,
      "io.scalaland"                  %%% "chimney"                        % Versions.chimney,
      "dev.cheleb"                    %%% "zio-jwt"                        % Versions.zioLaminarTapir
    )

  val staticFilesGeneratorDependencies =
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt"        % Versions.scopt,
      "com.lihaoyi"      %% "os-lib"       % Versions.osLib,
      "org.slf4j"         % "slf4j-simple" % Versions.slf4j
    )
}
