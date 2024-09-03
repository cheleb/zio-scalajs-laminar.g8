package $package$.domain

import zio.json.JsonCodec
import sttp.tapir.Schema
import zio.prelude.Debug
import zio.prelude.derivation.DebugGen

case class Dog(name: String, age: Int) derives JsonCodec, Schema

object Dog:
  given Debug[Dog] = DebugGen.derived[Dog]
