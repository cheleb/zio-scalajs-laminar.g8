package $package$.service

import zio.Task
import io.scalaland.chimney.dsl.*
import io.scalaland.chimney.Transformer

extension [A](task: Task[A])
  def mapInto[B](using Transformer[A, B]): Task[B] =
    task.map(_.into[B].transform)

extension [A](task: Task[Option[A]])
  def mapOption[B](using Transformer[A, B]): Task[Option[B]] =
    task.map(_.map(_.into[B].transform))
