import java.nio.charset.StandardCharsets
import org.scalajs.linker.interface.ModuleSplitStyle

import Dependencies._
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

lazy val generator = project
  .in(file("build/generator"))
  .enablePlugins(SbtTwirl)
  .disablePlugins(RevolverPlugin)
  .settings(staticFilesGeneratorDependencies)

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

Global / onLoad := {
  val scalaVersionValue = (client / scalaVersion).value
  val outputFile =
    target.value / "build-env.sh"
  if (!outputFile.exists()) {
    IO.writeLines(
      outputFile,
      s"""  
         |# Generated file see build.sbt
         |SCALA_VERSION="\$scalaVersionValue"
         |""".stripMargin.split("\n").toList,
      StandardCharsets.UTF_8
    )
  }
  (Global / onLoad).value
}
