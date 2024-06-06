package $package$.service

import zio.*

import io.scalaland.chimney.dsl._
import java.time.Instant
import java.time.ZonedDateTime

import $package$.domain.*

trait PersonService {
  def register(person: Person): Task[User]
}

class PersonServiceLive private extends PersonService {
  def register(person: Person): Task[User] =
    ZIO.succeed(person.into[User].withFieldComputed(_.creationDate, _ => ZonedDateTime.now()).transform)
}

object PersonServiceLive {
  val layer: ULayer[PersonService] = ZLayer.succeed(new PersonServiceLive)
}
