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
// - prod: production mode, aka with BFF and webjar deployment
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
      Seq(SbtWeb, SbtTwirl, JavaAppPackaging, WebScalaJSBundlerPlugin, DockerPlugin, AshScriptPlugin)
    case _ => Seq()
  }

  def scalaJSModule = mode match {
    case "prod" => ModuleKind.CommonJSModule
    case _      => ModuleKind.ESModule
  }

  def serverSettings(clientProjects: Project*) = mode match {
    case "prod" =>
      Seq(
        Compile / compile              := ((Compile / compile) dependsOn scalaJSPipeline).value,
        Assets / WebKeys.packagePrefix := "public/",
        Runtime / managedClasspath += (Assets / packageBin).value,
        scalaJSProjects         := clientProjects,
        Assets / pipelineStages := Seq(scalaJSPipeline)
      ) ++ dockerSettings
    case _ => Seq()
  }

  def staticGenerationSettings(generator: Project) =
    if (mode == "prod")
      Seq(
        Assets / resourceGenerators += Def
          .taskDyn[Seq[File]] {
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

$if(scalablytyped.truthy)$
  //
  // ScalablyTyped settings
  //
  val scalablyTypedPlugin = mode match {
    case "prod" => ScalablyTypedConverterPlugin
    case _      => ScalablyTypedConverterExternalNpmPlugin
  }
  
  val scalablytypedSettings = mode match {
    case "prod" =>
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
        // scala.sys.process.Process(List("npm", "install", "--silent", "--no-audit", "--no-fund"), baseDirectory.value).!
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
    case "prod" => ScalaJSBundlerPlugin
    case _      => ScalaJSPlugin
  }

  def symlink(link: File, target: File): Unit =
    if (!(Files.exists(link.toPath) || Files.isSymbolicLink(link.toPath)))
      if (Files.exists(target.toPath))
        Files.createSymbolicLink(link.toPath, link.toPath.getParent.relativize(target.toPath))

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

    val versionFile = baseDirectory / "version.sbt"
    if (!versionFile.exists()) {
      IO.write(versionFile, s"""ThisBuild / version := "$version$"""")
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
    )
  }

}