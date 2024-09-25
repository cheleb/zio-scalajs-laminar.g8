package $package$.repositories

import zio.*

import $package$.UserEntity
import $package$.NewUserEntity
import $package$.domain.PetType

import io.getquill.*
import io.getquill.jdbczio.*
import io.getquill.jdbczio.Quill.Postgres
import io.scalaland.chimney.dsl.*

import io.getquill.jdbczio.Quill.DataSource
import io.getquill.context.jdbc.PostgresJdbcTypes

trait UserRepository {
  def create(user: NewUserEntity): Task[UserEntity]
  def getById(id: Long): Task[Option[UserEntity]]
  def findByEmail(email: String): Task[Option[UserEntity]]
  def update(id: Long, op: UserEntity => UserEntity): Task[UserEntity]
  def delete(id: Long): Task[UserEntity]
}

class UserRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends UserRepository {

  import quill.*

  inline given SchemaMeta[NewUserEntity] = schemaMeta[NewUserEntity]("users")
  inline given InsertMeta[NewUserEntity] = insertMeta[NewUserEntity](_.id)
  inline given SchemaMeta[UserEntity]    = schemaMeta[UserEntity]("users")
  inline given UpdateMeta[UserEntity]    = updateMeta[UserEntity](_.id, _.creationDate)

  inline given MappedEncoding[PetType, String] =
    MappedEncoding[PetType, String](_.toString)
  inline given MappedEncoding[String, PetType] =
    MappedEncoding[String, PetType](PetType.valueOf)

  override def create(user: NewUserEntity): Task[UserEntity] =
    run(query[NewUserEntity].insertValue(lift(user)).returning(r => r))
      .map(r => r.intoPartial[UserEntity].transform.asOption)
      .someOrFail(new RuntimeException(""))
  override def getById(id: Long): Task[Option[UserEntity]] =
    run(query[UserEntity].filter(_.id == lift(id))).map(_.headOption)
  override def findByEmail(email: String): Task[Option[UserEntity]] =
    run(query[UserEntity].filter(_.email == lift(email))).map(_.headOption)

  override def update(id: Long, op: UserEntity => UserEntity): Task[UserEntity] =
    for {
      userEntity <- getById(id).someOrFail(new RuntimeException(s"User \$id not found"))
      updated <-
        run(
          query[UserEntity].filter(_.id == lift(userEntity.id)).updateValue(lift(op(userEntity))).returning(r => r)
        )
    } yield updated

  override def delete(id: Long): Task[UserEntity] =
    run(query[UserEntity].filter(_.id == lift(id)).delete.returning(r => r))
}

object UserRepositoryLive {
  def layer: RLayer[Postgres[SnakeCase], UserRepository] = ZLayer.derive[UserRepositoryLive]
}
