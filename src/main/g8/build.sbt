import java.nio.charset.StandardCharsets
import org.scalajs.linker.interface.ModuleSplitStyle

val scala3 = "$scala_version$"

val tapirVersion = "$tapir_version$"

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
  .settings(
    libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0",
    libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.10.1",
    libraryDependencies += "org.slf4j" % "slf4j-simple" % "2.0.13"
  )

val dev = sys.env.get("DEV").getOrElse("demo")

val serverPlugins = dev match {
  case "prod" =>
    Seq(SbtWeb, SbtTwirl, JavaAppPackaging, WebScalaJSBundlerPlugin)
  case _ => Seq()
}

def scalaJSModule = dev match {
  case "prod" => ModuleKind.CommonJSModule
  case _      => ModuleKind.ESModule
}

val serverSettings = dev match {
  case "prod" =>
    Seq(
      Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
      Assets / WebKeys.packagePrefix := "public/",
      Runtime / managedClasspath += (Assets / packageBin).value
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
  .settings(
    publish / skip := true
  )

val staticGenerationSettings =
  if (dev == "prod")
    Seq(
      Assets / resourceGenerators += Def
        .taskDyn[Seq[File]] {
          val baseDir = baseDirectory.value
          val rootFolder = (Assets / resourceManaged).value / "public"
          rootFolder.mkdirs()
          (generator / Compile / runMain)
            .toTask {
              Seq(
                "samples.BuildIndex",
                "--title",
                s""""Laminar Form Derivation v ${version.value}"""",
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
    scalaJSProjects := Seq(client),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    libraryDependencies ++= Seq(
      "io.github.iltotore" %% "iron-zio-json" % "2.5.0",
      "com.softwaremill.sttp.tapir" %% "tapir-zio" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % "test"
    )
  )
  .settings(serverSettings: _*)
  .dependsOn(sharedJvm, core)
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

lazy val client = scalajsProject("client", true)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { config =>
      dev match {
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
  .dependsOn(ui5, sharedJs)
  .settings(
    publish / skip := true
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/shared"))
  .settings(
    publish / skip := true
  )
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

Test / fork := false

def nexusNpmSettings =
  sys.env
    .get("NEXUS")
    .map(url =>
      npmExtraArgs ++= Seq(
        s"--registry=$url/repository/npm-public/"
      )
    )
    .toSeq

def scalaJSPlugin = dev match {
  case "prod" => ScalaJSBundlerPlugin
  case _      => ScalaJSPlugin
}

def scalajsProject(projectId: String): Project =
  Project(
    id = projectId,
    base = file(s"modules/$projectId")
  )
    .enablePlugins(scalaJSPlugin)
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
  val scalaVersionValue = (example / scalaVersion).value
  val outputFile =
    target.value / "build-env.sh"
  IO.writeLines(
    outputFile,
    s"""  
  |# Generated file see build.sbt
  |SCALA_VERSION="$scalaVersionValue"
  |""".stripMargin.split("\n").toList,
    StandardCharsets.UTF_8
  )

  (Global / onLoad).value
}
