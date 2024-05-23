package $package$.domain

import zio.json.JsonCodec
import sttp.tapir.Schema


case class Person(
    name: String,
    age: Int,
    pet: Either[Cat, Dog]
) derives JsonCodec, Schema
