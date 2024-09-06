package $package$.domain

import zio.json.JsonCodec
import sttp.tapir.Schema
import zio.prelude.Debug
import zio.prelude.magnolia.*

case class Cat(name: String) derives JsonCodec, Schema, Debug
