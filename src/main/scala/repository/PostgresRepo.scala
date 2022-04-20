package repository

import zio.*
import domain.{*, given}

trait PostgresRepo:
  def getChanges: Task[List[Change]]

object PostgresRepo:
  // accessor method
  def getChanges =
    ZIO.serviceWithZIO[PostgresRepo](_.getChanges)
