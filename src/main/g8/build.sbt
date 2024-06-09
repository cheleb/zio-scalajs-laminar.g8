import java.nio.charset.StandardCharsets
import org.scalajs.linker.interface.ModuleSplitStyle

import Dependencies._

val scala3 = "$scala_version$"

name := "$name$"

val laminarVersion = "$laminar_version$"

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
              s""""\${name.value} v \${version.value}"""",
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

lazy val server = project
  .in(file("modules/server"))
  .enablePlugins(serverPlugins: _*)
  .settings(
    staticGenerationSettings
  )
  .settings(
    fork := true,
    libraryDependencies ++= Seq(
      "io.github.iltotore"          %% "iron-zio-json"            % "2.5.0",
      "com.softwaremill.sttp.tapir" %% "tapir-zio"                % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"    % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server"   % Versions.tapir % "test"
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

val scalaJsSettings = mode match {
  case "prod" =>
    Seq(
    $if(scalablytyped.truthy)$
      Compile / npmDependencies ++= Seq(
        "chart.js"        -> "2.9.4",
        "@types/chart.js" -> "2.9.29"
      ),
    $endif$
      webpack / version      := "5.91.0",
      scalaJSStage in Global := FullOptStage,
      webpackBundlingMode    := BundlingMode.LibraryAndApplication()
    )
  case _ =>
    $if(scalablytyped.truthy)$
    Seq(externalNpm := {
      // scala.sys.process.Process(List("npm", "install", "--silent", "--no-audit", "--no-fund"), baseDirectory.value).!
      baseDirectory.value / "scalablytyped"
    })
    $else$
    Seq()
    $endif$
}
$endif$

lazy val client = scalajsProject("client")
  .enablePlugins(scalablyTypedPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { config =>
      mode match {
        case "prod" =>
          config
            .withModuleKind(scalaJSModule)
            .withMinify(true)
            .withOptimizer(true)
            .withClosureCompiler(true)

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
  .settings(
    scalaJsSettings
  )
  .dependsOn(sharedJs)
  .settings(
    publish / skip := true
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .disablePlugins(RevolverPlugin)
  .in(file("modules/shared"))
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client" % Versions.tapir,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"    % Versions.tapir,
      "com.softwaremill.sttp.client3" %% "zio"               % Versions.sttp,
      "io.scalaland"                 %%% "chimney"           % "1.1.0"
    )
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
