package $package$.config

import zio.Config
import zio.config.magnolia.deriveConfig

final case class FlywayConfig(url: String, user: String, password: String)

object FlywayConfig:
  given Config[FlywayConfig] = deriveConfig[FlywayConfig]
