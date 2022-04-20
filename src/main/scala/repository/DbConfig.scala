package repository

import zio.*
import javax.sql.DataSource

case class Config(url: String, port: String, password: String)

trait DbConfig:
  def loadConfig: ZLayer[Any, Throwable, DataSource]
