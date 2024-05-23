package $package$.domain

import zio.json.JsonCodec
import sttp.tapir.Schema

case class Dog(name: String, age: Int) derives JsonCodec, Schema
