package $package$.repositories

import zio.Task

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill

trait TransactionSupport(quill: Quill.Postgres[SnakeCase]) {
  def tx[A](zio: Task[A]): Task[A] = quill.transaction(zio)
}
