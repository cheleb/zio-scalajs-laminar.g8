package $package$.repositories

import zio.*

import $package$.PetEntity
import $package$.CatEntity
import $package$.DogEntity
import $package$.domain.PetType

import io.getquill.*
import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.Postgres

trait PetRepository {
  def create(pet: PetEntity): Task[PetEntity]
  def getCatById(id: Long): Task[Option[CatEntity]]
  def getDogById(id: Long): Task[Option[DogEntity]]
  def updateCat(id: Long, op: CatEntity => CatEntity): Task[CatEntity]
  def updateDog(id: Long, op: DogEntity => DogEntity): Task[DogEntity]
  def delete(petType: PetType, id: Long): Task[PetEntity]
}

class PetRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends PetRepository {

  import quill.*

  inline given SchemaMeta[CatEntity] = schemaMeta[CatEntity]("cats")
  inline given InsertMeta[CatEntity] = insertMeta[CatEntity](_.id)
  inline given UpdateMeta[CatEntity] = updateMeta[CatEntity](_.id, _.creationDate)

  inline given SchemaMeta[DogEntity] = schemaMeta[DogEntity]("dogs")
  inline given InsertMeta[DogEntity] = insertMeta[DogEntity](_.id)
  inline given UpdateMeta[DogEntity] = updateMeta[DogEntity](_.id, _.creationDate)

  override def create(pet: PetEntity): Task[PetEntity] = pet match

    case cat: CatEntity =>
      run(query[CatEntity].insertValue(lift(cat)).returning(r => r))
    case dog: DogEntity =>
      run(query[DogEntity].insertValue(lift(dog)).returning(r => r))

  override def getCatById(id: Long): Task[Option[CatEntity]] =
    run(query[CatEntity].filter(_.id == lift(id))).map(_.headOption)

  override def getDogById(id: Long): Task[Option[DogEntity]] =
    run(query[DogEntity].filter(_.id == lift(id))).map(_.headOption)

  override def updateCat(id: Long, op: CatEntity => CatEntity): Task[CatEntity] =
    for {
      pets <- getCatById(id).someOrFail(new RuntimeException(s"Pets \$id not found"))
      updated <- run(
                   query[CatEntity]
                     .filter(_.id == lift(pets.id))
                     .updateValue(lift(op(pets)))
                     .returning(r => r)
                 )
    } yield updated
  override def updateDog(id: Long, op: DogEntity => DogEntity): Task[DogEntity] =
    for {
      pets <- getDogById(id).someOrFail(new RuntimeException(s"Pets \$id not found"))
      updated <- run(
                   query[DogEntity]
                     .filter(_.id == lift(pets.id))
                     .updateValue(lift(op(pets).asInstanceOf[DogEntity]))
                     .returning(r => r)
                 )
    } yield updated

  override def delete(petType: PetType, id: Long): Task[PetEntity] = petType match
    case PetType.Cat =>
      run(query[CatEntity].filter(_.id == lift(id)).delete.returning(r => r))
    case PetType.Dog =>
      run(query[DogEntity].filter(_.id == lift(id)).delete.returning(r => r))

}

object PetRepositoryLive {
  def layer: RLayer[Postgres[SnakeCase], PetRepository] = ZLayer.derive[PetRepositoryLive]
}
