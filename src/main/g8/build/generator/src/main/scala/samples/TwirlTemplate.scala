package samples

import java.io.File

import io.*
import org.slf4j.LoggerFactory

case class Config(
    title: String = "",
    resourceManaged: File = new File("target/")
)

@main
def BuildIndex(args: String*): Unit = {
  val logger = LoggerFactory.getLogger("TwirlTemplate")

  val parser = new scopt.OptionParser[Config]("scopt") {
    head("scopt", "3.x")
    opt[String]("title")
      .action((title, config) =>
        config
          .copy(title = title)
      )
      .required()
    opt[File]("resource-managed")
      .action((srcManaged, config) =>
        config
          .copy(resourceManaged = srcManaged)
      )
      .required()
    help("help")
  }

  parser.parse(args, Config()) match {
    case None =>
      logger.error(parser.usage)
    case Some(config) =>
      config.resourceManaged.mkdirs()
      val indexFile = os.Path(config.resourceManaged) / "index.html"
      logger.info(s"Writing \$indexFile")
      os.write.over(
        indexFile,
        html.index.apply(config.title).body
      )

  }

}
