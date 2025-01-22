import sbt._
import sbt.Keys._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  val Versions = new {
    val chimney               = "1.6.0"
    val flywaydb              = "11.2.0"
    val frontroute            = "0.19.0"
    val iron                  = "2.6.0"
    val javaMail              = "1.6.2"
    val laminarFormDerivation = "0.21.1"
    val logback               = "1.5.16"
    val mUnit                 = "1.0.2"
    val osLib                 = "0.10.2"
    val postgresql            = "42.7.5"
    val quill                 = "4.8.6"
    val scopt                 = "4.1.0"
    val slf4j                 = "2.0.16"
    val stripe                = "25.10.0"
    val sttp                  = "3.9.6"
    val tapir                 = "1.11.11"
    val zio                   = "2.1.14"
    val zioConfig             = "4.0.2"
    val zioLaminarTapir       = "0.8.3"
    val zioLogging            = "2.2.4"
    val zioMock               = "1.0.0-RC12"
    val zioPrelude            = "1.0.0-RC36"
    val zioTestContainers     = "0.10.0"
  }

  private val configDependencies = Seq(
    "dev.zio" %% "zio-config"          % Versions.zioConfig,
    "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig,
    "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig
  )

  private val databaseDependencies = Seq(
    "org.flywaydb"           % "flyway-core"                       % Versions.flywaydb,
    "org.flywaydb"           % "flyway-database-postgresql"        % Versions.flywaydb,
    "org.postgresql"         % "postgresql"                        % Versions.postgresql,
    "io.github.scottweaver" %% "zio-2-0-testcontainers-postgresql" % Versions.zioTestContainers % Test
  )

  private val loggingDependencies = Seq(
    "dev.zio"       %% "zio-logging"       % Versions.zioLogging,
    "dev.zio"       %% "zio-logging-slf4j" % Versions.zioLogging,
    "ch.qos.logback" % "logback-classic"   % Versions.logback
  )

  private val quillDependencies = Seq(
    "io.getquill" %% "quill-jdbc-zio" % Versions.quill
  )

  private val jwtDependencies = Seq(
    "com.auth0" % "java-jwt" % "4.4.0"
  )

  val serverLibraryDependencies =
    libraryDependencies ++= Seq(
      "io.scalaland"                %% "chimney"                  % Versions.chimney,
      "com.softwaremill.sttp.tapir" %% "tapir-zio"                % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"    % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % Versions.tapir,
      "dev.cheleb"                  %% "zio-tapir-server"         % Versions.zioLaminarTapir,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server"   % Versions.tapir   % Test,
      "dev.zio"                     %% "zio-test"                 % Versions.zio,
      "dev.zio"                     %% "zio-test-junit"           % Versions.zio     % Test,
      "dev.zio"                     %% "zio-test-sbt"             % Versions.zio     % Test,
      "dev.zio"                     %% "zio-test-magnolia"        % Versions.zio     % Test,
      "dev.zio"                     %% "zio-mock"                 % Versions.zioMock % Test
    ) ++
      configDependencies ++
      databaseDependencies ++
      quillDependencies ++
      jwtDependencies ++
      loggingDependencies

  val testingLibraryDependencies =
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit"        % Versions.mUnit % Test,
      "dev.zio"       %% "zio-test"     % Versions.zio   % Test,
      "dev.zio"       %% "zio-test-sbt" % Versions.zio   % Test
    )

  val sharedJvmAndJsLibraryDependencies: Setting[Seq[ModuleID]] =
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %%% "tapir-zio"                      % Versions.tapir,
      "com.softwaremill.sttp.tapir" %%% "tapir-iron"                     % Versions.tapir,
      "com.softwaremill.sttp.tapir" %%% "tapir-json-zio"                 % Versions.tapir,
      "dev.cheleb"                  %%% "laminar-form-derivation-shared" % Versions.laminarFormDerivation,
      "dev.cheleb"                  %%% "zio-tapir-shared"               % Versions.zioLaminarTapir,
      "dev.zio"                     %%% "zio-prelude"                    % Versions.zioPrelude,
      "dev.zio"                     %%% "zio-prelude-magnolia"           % Versions.zioPrelude,
      "io.github.iltotore"           %% "iron-zio-json"                  % Versions.iron
    )

  val clientLibraryDependencies: Setting[Seq[ModuleID]] =
    libraryDependencies ++= Seq(
      // pull laminar 17.1.0
      "dev.cheleb" %%% "laminar-form-derivation-ui5" % Versions.laminarFormDerivation,
      // pull tapir-sttp-client and zio-tapir
      "dev.cheleb"    %%% "zio-tapir-laminar" % Versions.zioLaminarTapir,
      "io.frontroute" %%% "frontroute"        % Versions.frontroute
    )

  val clientAndServerLibraries = Seq(
  )

  val staticFilesGeneratorDependencies =
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt"        % Versions.scopt,
      "com.lihaoyi"      %% "os-lib"       % Versions.osLib,
      "org.slf4j"         % "slf4j-simple" % Versions.slf4j
    )
}
