import java.nio.charset.StandardCharsets
import org.scalajs.linker.interface.ModuleSplitStyle

import Dependencies._
//
// Will handle different build modes:
// - prod: production mode
// - demo: demo mode (default)
// - dev:  development mode
//
import ServerSettings._

val scala3 = "$scala_version$"

name := "$name$"

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

//
// This is static generation settings to be used in server project
// Illustrate how to use the generator project to generate static files with twirl
//
lazy val generator = project
  .in(file("build/generator"))
  .enablePlugins(SbtTwirl)
  .disablePlugins(RevolverPlugin)
  .settings(staticFilesGeneratorDependencies)

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
// Server project
// It depends on sharedJvm project, a project that contains shared code between server and client
//
lazy val server = project
  .in(file("modules/server"))
  .enablePlugins(serverPlugins: _*)
  .settings(
    staticGenerationSettings(generator)
  )
  .settings(
    fork := true,
    serverLibraryDependencies
  )
  .settings(serverSettings(client): _*)
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
// Client project
// It depends on sharedJs project, a project that contains shared code between server and client.
//
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
      // pull laminar 17.1.0
      "dev.cheleb"    %%% "laminar-form-derivation-ui5" % Versions.laminarFormDerivation,
      "dev.cheleb"    %%% "zio-laminar-tapir"           % Versions.zioLaminarTapir,
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
  .jsSettings(
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.5.0" // implementations of java.time classes for Scala.JS,
    )
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

//
// This is a global setting that will generate a build-env.sh file in the target directory.
// This file will contain the SCALA_VERSION variable that can be used in the build process
//
Global / onLoad := {

  // This is hack to share static files between server and client.
  // It creates symlinks from server to client static files
  // Ideally, we should use a shared folder for static files
  // Or use a shared CDN
  // Or copy the files to the target directory of the server at build time.

  symlink(server.base / "src" / "main" / "public" / "img", client.base / "img")
  symlink(server.base / "src" / "main" / "public" / "css", client.base / "css")

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
