package $package$.service

import zio.*

import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.Clock as JavaClock

import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.*
import com.auth0.jwt.algorithms.Algorithm
import $package$.config.Configs
import $package$.config.JWTConfig
import $package$.domain.*

trait JWTService {
  def createToken(user: User): Task[UserToken]
  def verifyToken(token: String): Task[UserID]
}

class JWTServiceLive private (jwtConfig: JWTConfig, clock: JavaClock) extends JWTService {

  val ISSUER = "rockthejvl.com"

  val secret: String  = jwtConfig.secret
  val algorithm       = Algorithm.HMAC512("secret")
  val TTL             = Duration.of(30, ChronoUnit.DAYS)
  val CLAIN_USER_NAME = "userName"

  private val verifier = JWT
    .require(algorithm)
    .withIssuer(ISSUER)
    .asInstanceOf[BaseVerification]
    .build(clock)

  override def createToken(user: User): Task[UserToken] =
    for {
      now      <- ZIO.attempt(clock.instant())
      userId   <- ZIO.fromOption(user.id).orElseFail(new RuntimeException("User ID is missing"))
      expiresAt = now.plus(TTL)
      token <- ZIO.attempt(
                 JWT
                   .create()
                   .withIssuer(ISSUER)
                   .withIssuedAt(now)
                   .withExpiresAt(expiresAt)
                   .withSubject(userId.toString)
                   .withClaim(CLAIN_USER_NAME, user.email)
                   .sign(algorithm)
               )
    } yield UserToken(userId, user.email, token, expiresAt.getEpochSecond)

  override def verifyToken(token: String): Task[UserID] =
    for {
      decoded <- ZIO.attempt(verifier.verify(token))
      userID <- ZIO.attempt(
                  UserID(decoded.getSubject().toLong, decoded.getClaim(CLAIN_USER_NAME).asString())
                )
    } yield userID
}

object JWTServiceLive {
  val layer = ZLayer(
    for
      jwtConfig <- ZIO.service[JWTConfig]
      clock     <- Clock.javaClock
    yield JWTServiceLive(jwtConfig, clock)
  )

  val configuredLayer =
    Configs.makeConfigLayer[JWTConfig]("jwt") >>> JWTServiceLive.layer
}
