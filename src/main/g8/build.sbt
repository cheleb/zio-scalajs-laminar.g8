import java.nio.charset.StandardCharsets
import org.scalajs.linker.interface.ModuleSplitStyle

val scala3 = "$scala_version$"

val tapirVersion = "$tapir_version$"

val laminarVersion = "$laminar_version$"

val Versions = new {
  val zio        = "2.1.1"
  val tapir      = "1.10.7"
  val zioLogging = "2.2.4"
  val zioConfig  = "4.0.2"
  val sttp       = "3.9.6"
  val javaMail   = "1.6.2"
  val stripe     = "25.7.0"
  val flywaydb   = "10.13.0"
}

inThisBuild(
  List(
    scalaVersion := scala3,
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Xfatal-warnings"
    )
  )
)

lazy val generator = project
  .in(file("build/generator"))
  .enablePlugins(SbtTwirl)
  .disablePlugins(RevolverPlugin)
  .settings(
    libraryDependencies += "com.github.scopt" %% "scopt"        % "4.1.0",
    libraryDependencies += "com.lihaoyi"      %% "os-lib"       % "0.10.1",
    libraryDependencies += "org.slf4j"         % "slf4j-simple" % "2.0.13"
  )

//
// Define the build mode:
// - prod: production mode
//         optimized, CommonJSModule
//         webjar packaging
// - demo: demo mode (default)
//         optimized, CommonJSModule
//         static files
// - dev:  development mode
//         no optimization, ESModule
//         static files, hot reload with vite.
//
// Default is "demo" mode, because the vite build does not take parameters.
//   (see vite.config.js)
val mode = sys.env.get("MOD").getOrElse("demo")

//
// On dev mode, server will only serve API and static files.
//
val serverPlugins = mode match {
  case "prod" =>
    Seq(SbtWeb, SbtTwirl, JavaAppPackaging, WebScalaJSBundlerPlugin)
  case _ => Seq()
}

def scalaJSModule = mode match {
  case "prod" => ModuleKind.CommonJSModule
  case _      => ModuleKind.ESModule
}

val serverSettings = mode match {
  case "prod" =>
    Seq(
      Compile / compile              := ((Compile / compile) dependsOn scalaJSPipeline).value,
      Assets / WebKeys.packagePrefix := "public/",
      Runtime / managedClasspath += (Assets / packageBin).value,
      scalaJSProjects         := Seq(client),
      Assets / pipelineStages := Seq(scalaJSPipeline)
    )
  case _ => Seq()
}

lazy val root = project
  .in(file("."))
  .aggregate(
    generator,
    server,
    sharedJs,
    sharedJvm,
    client
  )
  .disablePlugins(RevolverPlugin)
  .settings(
    publish / skip := true
  )

val staticGenerationSettings =
  if (mode == "prod")
    Seq(
      Assets / resourceGenerators += Def
        .taskDyn[Seq[File]] {
          val baseDir    = baseDirectory.value
          val rootFolder = (Assets / resourceManaged).value / "public"
          rootFolder.mkdirs()
          (generator / Compile / runMain).toTask {
            Seq(
              "samples.BuildIndex",
              "--title",
              s""""$name$ v \${version.value}"""",
              "--resource-managed",
              rootFolder
            ).mkString(" ", " ", "")
          }
            .map(_ => (rootFolder ** "*.html").get)
        }
        .taskValue
    )
  else
    Seq()

val commonDependencies = Seq(
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client" % Versions.tapir,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"    % Versions.tapir,
  "com.softwaremill.sttp.client3" %% "zio"               % Versions.sttp
)

lazy val server = project
  .in(file("modules/server"))
  .enablePlugins(serverPlugins: _*)
  .settings(
    staticGenerationSettings
  )
  .settings(
    fork := true,
    libraryDependencies ++= commonDependencies ++ Seq(
      "io.github.iltotore"          %% "iron-zio-json"            % "2.5.0",
      "com.softwaremill.sttp.tapir" %% "tapir-zio"                % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"    % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server"   % tapirVersion % "test"
    )
  )
  .settings(serverSettings: _*)
  .dependsOn(sharedJvm)
  .settings(
    publish / skip := true
  )

val usedScalacOptions = Seq(
  "-encoding",
  "utf8",
  "-unchecked",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xmax-inlines:64",
  "-Wunused:all"
)

$if(scalablytyped.truthy)$
//
// ScalablyTyped settings
//
val scalablyTypedPlugin = mode match {
  case "prod" => ScalablyTypedConverterPlugin
  case _      => ScalablyTypedConverterExternalNpmPlugin
}

val scalablyTypedNpmDependenciesSettings = mode match {
  case "prod" =>
    Seq(
      Compile / npmDependencies ++= Seq(
        "chart.js"        -> "2.9.4",
        "@types/chart.js" -> "2.9.29"
      )
    )
  case _ =>
    Seq(externalNpm := {
      // scala.sys.process.Process(List("npm", "install", "--silent", "--no-audit", "--no-fund"), baseDirectory.value).!
      baseDirectory.value / "sclaably-typed-external-npm"
    })
}
$endif$

lazy val client = scalajsProject("client")
  .enablePlugins(scalablyTypedPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { config =>
      mode match {
        case "prod" =>
          config.withModuleKind(scalaJSModule)
        case _ =>
          config
            .withModuleKind(scalaJSModule)
            .withSourceMap(false)
            .withModuleSplitStyle(ModuleSplitStyle.SmallestModules)
      }
    }
  )
  .settings(scalacOptions ++= usedScalacOptions)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %%% "tapir-sttp-client" % Versions.tapir,
      "com.softwaremill.sttp.tapir"   %%% "tapir-json-zio"    % Versions.tapir,
      "com.softwaremill.sttp.client3" %%% "zio"               % Versions.sttp,
      "dev.zio"                       %%% "zio-json"          % "0.6.2",
      "dev.zio"                       %%% "zio-prelude"       % "1.0.0-RC26",
      $if(scalariform.truthy)$
      // pull laminar 17.0.0
      "dev.cheleb"    %%% "laminar-form-derivation-ui5" % "0.12.0",
      $else$
      "com.raquo"     %%% "laminar"                     % laminarVersion,
      $endif$
      "io.frontroute" %%% "frontroute"                  % "0.19.0"
    )
  )
  $if(scalablytyped.truthy)$
  .settings(
    scalablyTypedNpmDependenciesSettings
  )
  $endif$
  .dependsOn(sharedJs)
  .settings(
    publish / skip := true
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .disablePlugins(RevolverPlugin)
  .in(file("modules/shared"))
  .settings(
    libraryDependencies ++= commonDependencies
  )
  .settings(
    publish / skip := true
  )
lazy val sharedJvm = shared.jvm
lazy val sharedJs  = shared.js

Test / fork := false

def nexusNpmSettings =
  sys.env
    .get("NEXUS")
    .map(url =>
      npmExtraArgs ++= Seq(
        s"--registry=\$url/repository/npm-public/"
      )
    )
    .toSeq

def scalaJSPlugin = mode match {
  case "prod" => ScalaJSBundlerPlugin
  case _      => ScalaJSPlugin
}

def scalajsProject(projectId: String): Project =
  Project(
    id = projectId,
    base = file(s"modules/\$projectId")
  )
    .enablePlugins(scalaJSPlugin)
    .disablePlugins(RevolverPlugin)
    .settings(nexusNpmSettings)
    .settings(Test / requireJsDomEnv := true)
    .settings(
      scalacOptions := Seq(
        "-scalajs",
        "-deprecation",
        "-feature",
        "-Xfatal-warnings"
      )
    )

Global / onLoad := {
  val scalaVersionValue = (client / scalaVersion).value
  val outputFile =
    target.value / "build-env.sh"
  IO.writeLines(
    outputFile,
    s"""  
       |# Generated file see build.sbt
       |SCALA_VERSION="\$scalaVersionValue"
       |""".stripMargin.split("\n").toList,
    StandardCharsets.UTF_8
  )

  (Global / onLoad).value
}
