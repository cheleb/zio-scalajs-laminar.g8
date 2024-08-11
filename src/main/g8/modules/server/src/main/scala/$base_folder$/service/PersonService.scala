package $package$.service

import zio.*

import io.scalaland.chimney.dsl._
import java.time.Instant
import java.time.ZonedDateTime

import $package$.domain.*

$if(quill.truthy)$
import $package$.repositories.UserRepository
$endif$

trait PersonService {
  def register(person: Person): Task[User]
}

class PersonServiceLive private $if(quill.truthy)$(userRepository: UserRepository)$endif$ extends PersonService {
  def register(person: Person): Task[User] =
    $if(quill.truthy)$
    userRepository.create(
      User(
        id = None,
        name = person.name,
        email = person.email,
        age = person.age,
        creationDate = ZonedDateTime.now()
      )
    )
    $else$
    ZIO.succeed(person.into[User].withFieldComputed(_.creationDate, _ => ZonedDateTime.now()).transform)
    $endif$
}

object PersonServiceLive {
  val layer: RLayer[UserRepository, PersonService] = ZLayer.derive[PersonServiceLive]
}
