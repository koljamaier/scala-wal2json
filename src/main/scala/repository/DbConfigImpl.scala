package repository

import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.{PostgresZioJdbcContext, SnakeCase}
import javax.sql.DataSource
import zio.Console.printLine
import zio.*

case class DbConfigImpl(console: Console, prefix: String)
    extends PostgresZioJdbcContext(SnakeCase),
      DbConfig:
  override def loadConfig: ZLayer[Any, Throwable, DataSource] =
    DataSourceLayer
      .fromPrefix(prefix)
      .tapError(e => console.printLine(s"Failed with $e"))
      .orDie

object DbConfigImpl:
  val live =
    (for c <- ZIO.service[Console]
    yield DbConfigImpl(c, "myDatabaseConfig")).toLayer

  val test =
    (for c <- ZIO.service[Console]
    yield DbConfigImpl(c, "myTestDatabaseConfig")).toLayer
