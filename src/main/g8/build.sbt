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
// Server project
// It depends on sharedJvm project, a project that contains shared code between server and client
//
lazy val server = project
  .in(file("modules/server"))
  .enablePlugins(serverPlugins: _*)
  .settings(
    staticGenerationSettings(generator, client)
  )
  .settings(
    fork := true,
    serverLibraryDependencies,
    testingLibraryDependencies
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
$if(scalablytyped.truthy)$
  .enablePlugins(scalablyTypedPlugin)
$endif$
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { config =>
      mode match {
        case "CommonJs" =>
          config
            .withModuleKind(scalaJSModule)
            .withMinify(true)
            .withOptimizer(true)
            .withClosureCompiler(true)

        case "ESModule" =>
          config
            .withModuleKind(scalaJSModule)

        case _ =>
          config
            .withModuleKind(scalaJSModule)
            .withSourceMap(false)
            .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("$package$")))
      }
    }
  )
  .settings(scalacOptions ++= usedScalacOptions)
  .settings(clientLibraryDependencies)
$if(scalablytyped.truthy)$
  .settings(
    scalablytypedSettings
  )
$endif$
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

  insureBuildEnvFile(baseDirectory.value, (client / scalaVersion).value)

  // This is hack to share static files between server and client.
  // It creates symlinks from server to client static files
  // Ideally, we should use a shared folder for static files
  // Or use a shared CDN
  // Or copy the files to the target directory of the server at build time.
  symlink(server.base / "src" / "main" / "public" / "img", client.base / "img")
  symlink(server.base / "src" / "main" / "public" / "css", client.base / "css")
  symlink(server.base / "src" / "main" / "public" / "res", client.base / "res")

  symlink(server.base / "src" / "main" / "resources" / "public" / "img", client.base / "img")
  symlink(server.base / "src" / "main" / "resources" / "public" / "css", client.base / "css")
  symlink(server.base / "src" / "main" / "resources" / "public" / "res", client.base / "res")
  (Global / onLoad).value
}
