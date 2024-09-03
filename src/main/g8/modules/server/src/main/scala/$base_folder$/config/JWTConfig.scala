package $package$.config

import zio.Config
import zio.Duration
import zio.config.magnolia.deriveConfig

final case class JWTConfig(secret: String, issuer: String, ttl: Duration)

object JWTConfig:
  given Config[JWTConfig] = deriveConfig[JWTConfig]
