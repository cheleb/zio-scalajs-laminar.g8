import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.web._
import com.typesafe.sbt.web.Import._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalablytyped.converter.plugin._
import org.scalablytyped.converter.plugin.STKeys._
import org.scalajs.sbtplugin._
import play.twirl.sbt.SbtTwirl
import sbt._
import sbt.Keys._
import scalajsbundler.sbtplugin.WebScalaJSBundlerPlugin
import webscalajs.WebScalaJS.autoImport._
import scalajsbundler.sbtplugin._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object ServerSettings {
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

  def serverSettings(clientProjects: Project*) = mode match {
    case "prod" =>
      Seq(
        Compile / compile              := ((Compile / compile) dependsOn scalaJSPipeline).value,
        Assets / WebKeys.packagePrefix := "public/",
        Runtime / managedClasspath += (Assets / packageBin).value,
        scalaJSProjects         := clientProjects,
        Assets / pipelineStages := Seq(scalaJSPipeline)
      )
    case _ => Seq()
  }

  def staticGenerationSettings(generator: Project) =
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

}
