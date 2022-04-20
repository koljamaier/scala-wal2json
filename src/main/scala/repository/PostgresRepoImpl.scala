package repository

import zio.*
import domain.{*, given}
import io.getquill.{PostgresZioJdbcContext, SnakeCase}
import io.getquill.*
import io.getquill.context.ZioJdbc.*
import io.getquill.Dsl.autoQuote
import io.getquill.autoQuote
import sourcecode.Text.generate
import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField, given}
import zio.json.DecoderOps

import java.sql.SQLException
import javax.sql.DataSource

case class PostgresRepoLive(
    console: Console,
    clock: Clock,
    dataBaseConfig: DbConfig
) extends PostgresZioJdbcContext(SnakeCase),
      PostgresRepo:

  def log(line: String): UIO[Unit] =
    for {
      current <- clock.currentDateTime
      _       <- console.printLine(current.toString + "--" + line).orDie
    } yield ()

  val dataSourceLayer =
    dataBaseConfig.loadConfig

  override def getChanges =
    val query = quote { (replicationSlot: String) =>
      infix"""
            SELECT data
            FROM pg_logical_slot_get_changes(${replicationSlot}, NULL, NULL, 'include-xids', '0')"""
        .as[Query[String]]
    }
    run(query("test_slot"))
      .tap(s => log(s"Raw changes from Postgres: ${s.mkString}"))
      .map {
        case s if !s.isEmpty =>
          s.mkString
            .fromJson[ChangeLog]
            .fold(
              l => throw new RuntimeException("Could not parse"),
              r => r.changes
            )
        case _ => List()
      }
      .tapError(e => log(s"Error: ${e.toString}"))
      .tap(e => log(s"Parsed Change: ${e.toString}"))
      .provide(dataSourceLayer)

object PostgresRepoLive:
  val layer: URLayer[Console with Clock with DbConfig, PostgresRepo] =
    (PostgresRepoLive(_, _, _)).toLayer[PostgresRepo]
