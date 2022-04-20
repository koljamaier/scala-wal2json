import zio.json.{*, given}
import zio.json.JsonCodec.apply
import zio.*

import scala.io.Source
import domain.*
import domain.given
import repository.{DbConfigImpl, PostgresRepo, PostgresRepoLive}
import zio.Console.printLine
import zio.logging.*
import zio.stream.ZStream

object PgPollingConsumer extends ZIOAppDefault:

  val changes
      : ZStream[PostgresRepo with Clock with Console, Throwable, Change] =
    ZStream
      .repeatZIOWithSchedule(
        PostgresRepo.getChanges,
        Schedule.spaced(3.seconds)
      )
      .mapConcat(identity)

  def run =
    (for {
      change <- changes.runCollect
    } yield change)
      .provide(
        PostgresRepoLive.layer,
        Clock.live,
        Console.live,
        DbConfigImpl.live
      )
