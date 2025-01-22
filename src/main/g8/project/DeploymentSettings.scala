import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.archetypes.scripts.AshScriptPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.web._
import com.typesafe.sbt.web.Import._

import java.nio.charset.StandardCharsets
import java.nio.file.Files

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalablytyped.converter.plugin._
import org.scalablytyped.converter.plugin.STKeys._
import org.scalajs.sbtplugin._

import play.twirl.sbt.SbtTwirl

import sbt._
import sbt.Keys._

import scalajsbundler.sbtplugin._
import scalajsbundler.sbtplugin.WebScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

import webscalajs.WebScalaJS.autoImport._

object DeploymentSettings {
//
// Define the build mode:
// - CommonJs: production mode, aka with BFF and webjar deployment
//         optimized, CommonJSModule
//         webjar packaging
// - ESModule: demo mode (default)
//         optimized, CommonJSModule
//         static files
// - dev:  development mode
//         no optimization, ESModule
//         static files, hot reload with vite.
//
// Default is "demo" mode, because the vite build does not take parameters.
//   (see vite.config.js)
  val mode = sys.env.get("MOD").getOrElse("demo")

  val overrideDockerRegistry = sys.env.get("LOCAL_DOCKER_REGISTRY").isDefined

  val publicFolder = "public"

//
// On dev mode, server will only serve API and static files.
//
  val serverPlugins = mode match {
    case "CommonJs" =>
      Seq(SbtWeb, SbtTwirl, JavaAppPackaging, WebScalaJSBundlerPlugin, DockerPlugin, AshScriptPlugin)
    case "ESModule" =>
      Seq(SbtTwirl, JavaAppPackaging, DockerPlugin, AshScriptPlugin)
    case _ => Seq()
  }

  def scalaJSModule = mode match {
    case "CommonJs" => ModuleKind.CommonJSModule
    case _          => ModuleKind.ESModule
  }

  def serverSettings(clientProjects: Project*) = mode match {
    case "CommonJs" =>
      Seq(
        Compile / compile              := ((Compile / compile) dependsOn scalaJSPipeline).value,
        Assets / WebKeys.packagePrefix := s"$publicFolder/",
        Runtime / managedClasspath += (Assets / packageBin).value,
        scalaJSProjects         := clientProjects,
        Assets / pipelineStages := Seq(scalaJSPipeline)
      ) ++ dockerSettings
    case "ESModule" => dockerSettings
    case _          => Seq()
  }

  def staticGenerationSettings(generator: Project, client: Project) = mode match {
    case "CommonJs" =>
      Seq(
        Assets / resourceGenerators += Def
          .taskDyn[Seq[File]] {
            val rootFolder = (Assets / resourceManaged).value / publicFolder
            rootFolder.mkdirs()
            (generator / Compile / runMain).toTask {
              Seq(
                "samples.BuildIndex",
                "--title",
                s""""\${name.value} v2 \${version.value}"""",
                "--version",
                version.value,
                "--resource-managed",
                rootFolder
              ).mkString(" ", " ", "")
            }
              .map(_ => (rootFolder ** "*.html").get)
          }
          .taskValue
      )
    case "ESModule" =>
      Seq(
        (Compile / resourceGenerators) += Def
          .taskDyn[Seq[File]] {
            val rootFolder = (Compile / resourceManaged).value / publicFolder
            rootFolder.mkdirs()

            Def.task {
              if (
                scala.sys.process
                  .Process(
                    List("npm", "run", "build", "--", "--emptyOutDir", "--outDir", rootFolder.getAbsolutePath),
                    (client / baseDirectory).value
                  )
                  .! == 0
              ) {
                println(s"Generated static files in \${rootFolder}")
                (rootFolder ** "*.*").get
              } else {
                println(s"Failed to generate static files in \${rootFolder}")
                throw new IllegalStateException("Vite build failed")
              }

            }

          }
          .taskValue
      )
    case _ =>
      Seq()
  }

$if(scalablytyped.truthy)$
  //
  // ScalablyTyped settings
  //
  val scalablyTypedPlugin = mode match {
    case "CommonJs" => ScalablyTypedConverterPlugin
    case _      => ScalablyTypedConverterExternalNpmPlugin
  }
  
  val scalablytypedSettings = mode match {
    case "CommonJs" =>
      Seq(
        Compile / npmDependencies ++= Seq(
          "chart.js"        -> "$chartjs_version$",
          "@types/chart.js" -> "$chartts_version$"
        ),
        webpack / version      := "$webpack_version$",
        scalaJSStage in Global := FullOptStage,
        webpackBundlingMode    := BundlingMode.LibraryAndApplication()
      )
    case _ =>
      Seq(externalNpm := {
        baseDirectory.value / "scalablytyped"
      })
  }
$endif$

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
    case "CommonJs" => ScalaJSBundlerPlugin
    case _          => ScalaJSPlugin
  }

  def symlink(link: File, target: File): Unit = {
    if (!(Files.exists(link.getParentFile.toPath)))
      Files.createDirectories(link.getParentFile.toPath)
    if (!(Files.exists(link.toPath) || Files.isSymbolicLink(link.toPath)))
      if (Files.exists(target.toPath))
        Files.createSymbolicLink(link.toPath, link.toPath.getParent.relativize(target.toPath))
  }
  def insureBuildEnvFile(baseDirectory: File, scalaVersion: String) = {

    val outputFile = baseDirectory / "scripts" / "target" / "build-env.sh"

    val mainJSFile = "modules/client/target/scala-\$SCALA_VERSION/client-fastopt/main.js"
    lazy val buildFileContent = s"""
                                   |#!/bin/usr/env bash
                                   |
                                   |# This file is generated by build.sbt
                                   |#- On \${java.time.Instant.now}
                                   |# Do not edit it manually
                                   |
                                   |SCALA_VERSION="\$scalaVersion"
                                   |MAIN_JS_FILE=\$mainJSFile
                                   |""".stripMargin.split("\n").toList
    def writeBuildEnvFile(): Unit =
      IO.writeLines(
        outputFile,
        buildFileContent,
        StandardCharsets.UTF_8
      )

    if (
      outputFile.exists() && buildFileContent
        .filterNot(_.startsWith("#-")) == IO.readLines(outputFile, StandardCharsets.UTF_8).filterNot(_.startsWith("#-"))
    ) {
      println("build-env.sh file is up to date")
    } else {
      writeBuildEnvFile()
    }

  }

  lazy val dockerSettings = {
    import DockerPlugin.autoImport._
    import DockerPlugin.globalSettings._
    import sbt.Keys._
    Seq(
      Docker / maintainer     := "Joh doe",
      Docker / dockerUsername := Some("johndoe"),
      Docker / packageName    := "$projectId$",
      dockerBaseImage         := "azul/zulu-openjdk-alpine:23-latest",
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

}
