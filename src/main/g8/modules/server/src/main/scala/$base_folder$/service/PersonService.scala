package $package$.service

import zio.*
import $package$.domain.*

trait PersonService {
  def register(person: Person): Task[Person]
}

class PersonServiceLive private extends PersonService {
  def register(person: Person): Task[Person] = ZIO.succeed(person)
}

object PersonServiceLive {
  val layer: ULayer[PersonService] = ZLayer.succeed(new PersonServiceLive)
}