package $package$

import java.time.ZonedDateTime
import $package$.domain.*
import io.scalaland.chimney.Transformer

case class NewUserEntity(
  id: Option[Long],
  name: String,
  email: String,
  petType: Option[PetType],
  petId: Option[Long],
  hashedPassword: String,
  age: Int,
  creationDate: ZonedDateTime
)

case class UserEntity(
  id: Long,
  name: String,
  email: String,
  petType: Option[PetType],
  petId: Option[Long],
  hashedPassword: String,
  age: Int,
  creationDate: ZonedDateTime
)

object UserEntity:
  given Transformer[UserEntity, User] = Transformer.derive

sealed trait PetEntity {
  val id: Long
  def name: String
  def creationDate: ZonedDateTime
}

object PetEntity:
  def apply(pet: Pet): PetEntity = pet match
    case Cat(name)      => CatEntity(-1, name, ZonedDateTime.now())
    case Dog(name, age) => DogEntity(-1, name, age, ZonedDateTime.now())

case class CatEntity(id: Long, name: String, creationDate: ZonedDateTime) extends PetEntity
object CatEntity:
  given Transformer[CatEntity, Cat] = Transformer.derive

case class DogEntity(id: Long, name: String, age: Int, creationDate: ZonedDateTime) extends PetEntity
object DogEntity:
  given Transformer[DogEntity, Dog] = Transformer.derive
