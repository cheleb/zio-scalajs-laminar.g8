import org.scalajs.linker.interface.ModuleSplitStyle

import Dependencies._
//
// Will handle different build modes:
// - prod: production mode, aka with BFF and webjar deployment
// - demo: demo mode (default)
// - dev:  development mode
//
import DeploymentSettings._

val scala3 = "$scala_version$"

name := "$name$"

inThisBuild(
  List(
    scalaVersion      := scala3,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
//      "-Xfatal-warnings"
    ),
    run / fork := true,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
)

//
// This is static generation settings to be used in server project
// Illustrate how to use the generator project to generate static files with twirl
//
lazy val generator = project
  .in(file("build/generator"))
  .enablePlugins(SbtTwirl)
  .disablePlugins(RevolverPlugin)
  .settings(staticFilesGeneratorDependencies)
  .settings(
    publish / skip := true
  )

// Aggregate root project
// This is the root project that aggregates all other projects
// It is used to run tasks on all projects at once.
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

//
// Client project
// It depends on sharedJs project, a project that contains shared code between server and client.
//
lazy val client = scalajsProject("client")
$if(scalablytyped.truthy)$
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
$endif$
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { config =>
      mode match {
        case "ESModule" =>
          config
            .withModuleKind(ModuleKind.ESModule)
        case _ =>
          config
            .withModuleKind(ModuleKind.ESModule)
            .withSourceMap(false)
            .withModuleSplitStyle(ModuleSplitStyle.FewestModules)
      }
    }
  )
  .settings(scalacOptions ++= usedScalacOptions)
  .settings(clientLibraryDependencies)
$if(scalablytyped.truthy)$
  .settings(externalNpm := baseDirectory.value / "scalablytyped")
$endif$
  .dependsOn(sharedJs)
  .settings(
    publish / skip := true
  )

//
// Server project
// It depends on sharedJvm project, a project that contains shared code between server and client
//
lazy val server = project
  .in(file("modules/server"))
  .enablePlugins(SbtTwirl, SbtWeb, JavaAppPackaging, DockerPlugin, AshScriptPlugin)
  .settings(
    staticGenerationSettings(generator, client)
  )
  .settings(
    fork := true,
    serverLibraryDependencies,
    testingLibraryDependencies
  )
  .settings(dockerSettings)
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

//
// Shared project
// It is a cross project that contains shared code between server and client
//
lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .disablePlugins(RevolverPlugin)
  .in(file("modules/shared"))
  .settings(
    sharedJvmAndJsLibraryDependencies
  )
  .settings(
    publish / skip := true
  )
lazy val sharedJvm = shared.jvm
lazy val sharedJs  = shared.js

Test / fork := false

def scalajsProject(projectId: String): Project =
  Project(
    id = projectId,
    base = file(s"modules/\$projectId")
  )
    .enablePlugins(ScalaJSPlugin)
    .disablePlugins(RevolverPlugin)
    .settings(Test / requireJsDomEnv := true)
    .settings(
      scalacOptions := Seq(
        "-scalajs",
        "-deprecation",
        "-feature",
        "-Xfatal-warnings"
      )
    )

lazy val dockerSettings = {
  import DockerPlugin.autoImport._
  import DockerPlugin.globalSettings._
  import sbt.Keys._
  Seq(
    Docker / maintainer     := "Joh doe",
    Docker / dockerUsername := Some("$dockerRepo$"),
    Docker / packageName    := "$projectId$",
    dockerBaseImage         := "azul/zulu-openjdk-alpine:24-latest",
    dockerRepository        := Some("$dockerRegistry$"),
    dockerUpdateLatest      := true,
    dockerExposedPorts      := Seq(8000)
  ) ++ (overrideDockerRegistry match {
    case true =>
      Seq(
        Docker / dockerRepository := Some("registry.orb.local"),
        Docker / dockerUsername   := Some("zio-laminar-demo")
      )
    case false =>
      Seq()
  })
}

//
// This is a global setting that will generate a build-env.sh file in the target directory.
// This file will contain the SCALA_VERSION variable that can be used in the build process
//
Global / onLoad := {

  insureBuildEnvFile(baseDirectory.value, (client / scalaVersion).value)

  (Global / onLoad).value
}
