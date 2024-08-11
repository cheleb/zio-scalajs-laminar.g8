package $package$.repositories

import zio.*

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.getquill.jdbczio.Quill.Postgres
import javax.sql.DataSource

object Repository {

  def quillLayer: URLayer[DataSource, Postgres[SnakeCase.type]] = Quill.Postgres.fromNamingStrategy(SnakeCase)

  private def datasourceLayer: TaskLayer[DataSource] = Quill.DataSource.fromPrefix("db")

  def dataLayer: TaskLayer[Postgres[SnakeCase.type]] = datasourceLayer >>> quillLayer
}
