package $package$.repositories

import zio.*

import $package$.domain.User
import io.getquill.*
import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.Postgres

trait UserRepository {
  def create(user: User): Task[User]
  def getById(id: Long): Task[Option[User]]
  def getByEmail(email: String): Task[Option[User]]
  def update(id: Long, op: User => User): Task[User]
  def delete(id: Long): Task[User]
}

class UserRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends UserRepository {

  import quill.*

  inline given SchemaMeta[User] = schemaMeta[User]("users")
  inline given InsertMeta[User] = insertMeta[User](_.id)
  inline given UpdateMeta[User] = updateMeta[User](_.id, _.creationDate)

  override def create(user: User): Task[User] =
    run(query[User].insertValue(lift(user)).returning(r => r))
  override def getById(id: Long): Task[Option[User]] =
    run(query[User].filter(_.id == lift(Option(id)))).map(_.headOption)
  override def getByEmail(email: String): Task[Option[User]] =
    run(query[User].filter(_.email == lift(email))).map(_.headOption)

  override def update(id: Long, op: User => User): Task[User] =
    for {
      user <- getById(id).someOrFail(new RuntimeException(s"User \$id not found"))
      updated <- run(
                   query[User].filter(_.id == lift(user.id)).updateValue(lift(op(user))).returning(r => r)
                 )
    } yield updated

  override def delete(id: Long): Task[User] =
    run(query[User].filter(_.id == lift(Option(id))).delete.returning(r => r))
}

object UserRepositoryLive {
  def layer: RLayer[Postgres[SnakeCase], UserRepositoryLive] = ZLayer.derive[UserRepositoryLive]
}
