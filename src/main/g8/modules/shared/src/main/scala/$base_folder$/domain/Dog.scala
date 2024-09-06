package $package$.domain

import zio.json.JsonCodec
import sttp.tapir.Schema
import zio.prelude.Debug
import zio.prelude.magnolia.*

case class Dog(name: String, age: Int) derives JsonCodec, Schema, Debug
