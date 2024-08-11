package $package$.services

import zio.*
import org.flywaydb.core.Flyway

import $package$.config.FlywayConfig
import $package$.config.Configs

trait FlywayService {
  def runClean(): Task[Unit]
  def runBaseline(): Task[Unit]
  def runMigrations(): Task[Unit]
  def runRepair(): Task[Unit]
}

class FlywayServiceLive private (flyway: Flyway) extends FlywayService {
  override def runClean(): Task[Unit]      = ZIO.attemptBlocking(flyway.clean())
  override def runBaseline(): Task[Unit]   = ZIO.attemptBlocking(flyway.baseline())
  override def runMigrations(): Task[Unit] = ZIO.attemptBlocking(flyway.migrate())
  override def runRepair(): Task[Unit]     = ZIO.attemptBlocking(flyway.repair())
}

object FlywayServiceLive {
  def live: ZLayer[FlywayConfig, Throwable, FlywayService] = ZLayer(
    for {
      config <- ZIO.service[FlywayConfig]
      flyway <- ZIO.attempt(Flyway.configure().dataSource(config.url, config.user, config.password).load())
    } yield new FlywayServiceLive(flyway)
  )

  val configuredLayer = Configs.makeConfigLayer[FlywayConfig]("db.dataSource") >>> live
}
