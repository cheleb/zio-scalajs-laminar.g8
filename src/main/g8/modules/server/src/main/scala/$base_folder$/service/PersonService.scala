package $package$.service

import zio.*

import dev.cheleb.ziojwt.Hasher

import io.scalaland.chimney.dsl._
import java.time.Instant
import java.time.ZonedDateTime

import $package$.domain.*
import $package$.domain.errors.*
import $package$.login.LoginPassword
import $package$.repositories.UserRepository

import java.sql.SQLException

$if(quill.truthy)$
import $package$.repositories.UserRepository
$endif$

trait PersonService {
  def register(person: Person): Task[User]
  def login(email: String, password: String): Task[User]
  def getProfile(userId: UserID): Task[User]
}

class PersonServiceLive private (userRepository: UserRepository, jwtService: JWTService) extends PersonService {

  def register(person: Person): Task[User] =
    if person.age < 18 then ZIO.fail(TooYoungException(person.age))
    else
      $if(quill.truthy)$
      userRepository
        .create(
          User(
            id = None,
            name = person.name,
            email = person.email,
            hashedPassword = Hasher.generatedHash(person.password.toString),
            age = person.age,
            creationDate = ZonedDateTime.now()
          )
        )
        .catchSome { case e: SQLException =>
          ZIO.logError(s"Error code: \${e.getSQLState} while creating user: \${e.getMessage}")
            *> ZIO.fail(UserAlreadyExistsException())
        }
      $else$
      ZIO.succeed(person.into[User].withFieldComputed(_.creationDate, _ => ZonedDateTime.now()).transform)
      $endif$

  override def login(email: String, password: String): Task[User] =
    userRepository
      .findByEmail(email)
      .map {
        _.filter(user => Hasher.validateHash(password, user.hashedPassword))
      }
      .someOrFail(InvalidCredentialsException())

  override def getProfile(userId: UserID): Task[User] =
    userRepository.findByEmail(userId.email).someOrFail(UserNotFoundException(userId.email))

}

object PersonServiceLive {
  val layer: RLayer[UserRepository & JWTService, PersonService] = ZLayer.derive[PersonServiceLive]
}
