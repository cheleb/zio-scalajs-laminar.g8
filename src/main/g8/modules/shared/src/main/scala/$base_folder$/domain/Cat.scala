package $package$.domain


import zio.json.JsonCodec
import sttp.tapir.Schema

case class Cat(name: String) derives JsonCodec, Schema
