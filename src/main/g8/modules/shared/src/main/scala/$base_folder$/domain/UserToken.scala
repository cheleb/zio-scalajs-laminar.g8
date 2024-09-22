package $package$.domain

import zio.json.JsonCodec

import dev.cheleb.ziojwt.WithToken

final case class UserToken(id: Long, email: String, token: String, expiration: Long) extends WithToken
    derives JsonCodec
