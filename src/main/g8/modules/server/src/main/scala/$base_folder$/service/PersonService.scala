package $package$.service

import zio.*

import io.scalaland.chimney.dsl._
import java.time.Instant
import java.time.ZonedDateTime

import $package$.domain.*
import $package$.domain.errors.*

import $package$.repositories.UserRepository

trait PersonService {
  def register(person: Person): Task[User]
}

class PersonServiceLive private (userRepository: UserRepository) extends PersonService {
  def register(person: Person): Task[User] =
    if person.age < 18 then ZIO.fail(TooYoungException(person.age))
    else
      userRepository.create(
        User(
          id = None,
          name = person.name,
          email = person.email,
          age = person.age,
          creationDate = ZonedDateTime.now()
        )
      )
}

object PersonServiceLive {
  val layer: RLayer[UserRepository, PersonService] = ZLayer.derive[PersonServiceLive]
}
