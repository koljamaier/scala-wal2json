import org.junit.Test
import org.junit.Assert.*
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.testEnvironment.*
import zio.test.liveEnvironment.*
import zio.Console.*
import com.dimafeng.testcontainers.{
  DockerComposeContainer,
  ExposedService,
  FixedHostPortGenericContainer,
  Services,
  WaitingForService
}
import repository.{DbConfigImpl, PostgresRepo, PostgresRepoLive}
import domain.{Change, ChangeLog}
import domain.{*, given}
import org.testcontainers.containers.wait.strategy.{WaitStrategy, Wait}
import zio.json.ast.Json
import zio.json.DecoderOps
import java.time.Duration

import io.getquill.*
import io.getquill.context.ZioJdbc.*
import io.getquill.Dsl.autoQuote
import io.getquill.autoQuote

import java.io.File
import javax.sql.DataSource
import scala.io.Source

object TestContainer {
  def dockerCompose: ZLayer[ZEnv, Nothing, DockerComposeContainer] =
    ZManaged.acquireReleaseWith {
      ZIO
        .attemptBlocking {
          val container = new DockerComposeContainer(
            new File("docker/docker-compose.yaml"),
            Seq(
              ExposedService(
                "db",
                5432,
                Wait.forLogMessage(
                  ".*LOG:  database system is ready to accept connections.*",
                  1
                ),
                None
              )
            )
          )

          container.start()
          container
        }
        .tapError(e => Console.printLine(e))
        .orDie
    }(container =>
      ZIO
        .attemptBlocking(container.stop())
        .tapError(e => Console.printLine(e))
        .orDie
    ).toLayer
}

object QuillContext extends PostgresZioJdbcContext(SnakeCase)

case class PostgresRepoTest() extends PostgresRepo:
  override def getChanges =
    Task {
      val c = Change(
        "test",
        "test",
        "test",
        List("column", "names"),
        List("column", "types"),
        List(Json.Null)
      )
      List(c)
    }

object PostgresRepoTest {
  val test = ZLayer.succeed(new PostgresRepoTest)
}

case class TestTable(id: String, code: String)

object PostgresSpec extends zio.test.DefaultRunnableSpec:
  def spec = suite("PostgresSpec")(
    test("Initially it should be empty") {
      (for {
        output <- PostgresRepo.getChanges
      } yield assert(output)(equalTo(List[Change]())))
        .provideSome[TestEnvironment with DockerComposeContainer](
          DbConfigImpl.test,
          PostgresRepoLive.layer
        )
    },
    test("Watch for changes after inserting into table") {
      import QuillContext.*
      (for {
        db <- ZIO.service[DbConfigImpl]
        dbLayer = db.loadConfig
        _ <- QuillContext
          .run(
            quote(query[TestTable]).insertValue(lift(TestTable("test", "test")))
          )
          .provide(dbLayer)

        output <- PostgresRepo.getChanges
      } yield assert(output)(
        equalTo(
          List[Change](
            Change(
              "insert",
              "public",
              "test_table",
              List("id", "code"),
              List("text", "text"),
              List(Json.Str("test"), Json.Str("test"))
            )
          )
        )
      ))
        .provideSome[TestEnvironment with DockerComposeContainer](
          DbConfigImpl.test,
          PostgresRepoLive.layer
        )
    },
    test("Test test layers static `getChanges` response should match") {
      (for {
        output <- PostgresRepo.getChanges
      } yield assert(output)(
        equalTo(
          List[Change](
            Change(
              "test",
              "test",
              "test",
              List("column", "names"),
              List("column", "types"),
              List(Json.Null)
            )
          )
        )
      ))
        .provideSome(
          PostgresRepoTest.test
        )
    }
  ).provideCustomLayerShared(TestContainer.dockerCompose)

object ZioJsonSpec extends zio.test.DefaultRunnableSpec:
  def spec = suite("ZioJsonSpec")(
    test("Parsing wal_log.json") {
      val jsonPath = "src/test/resources/wal_log.json"
      val parsedJson =
        Source.fromFile(jsonPath).getLines.mkString.fromJson[ChangeLog]
      assertM(ZIO.fromEither(parsedJson))(
        equalTo(
          ChangeLog(
            List(
              Change(
                "insert",
                "public",
                "table_with_pk",
                List("a", "b", "c"),
                List(
                  "integer",
                  "character varying(30)",
                  "timestamp without time zone"
                ),
                List(
                  Json.Num(1),
                  Json.Str("Backup and Restore"),
                  Json.Str("2022-04-18 20:40:44.558034")
                )
              )
            )
          )
        )
      )
    }
  )
