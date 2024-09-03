package $package$.domain

import zio.json.JsonCodec
import sttp.tapir.Schema
import zio.prelude.Debug
import zio.prelude.derivation.DebugGen

case class Cat(name: String) derives JsonCodec, Schema

object Cat:
  given Debug[Cat] = DebugGen.derived[Cat]
