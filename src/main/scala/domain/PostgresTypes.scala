package domain

import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

case class Change(
    kind: String,
    schema: String,
    table: String,
    @jsonField("columnnames")
    columnNames: List[String],
    @jsonField("columntypes")
    columnTypes: List[String],
    @jsonField("columnvalues")
    columnValues: List[Json]
)

case class ChangeLog(
    @jsonField("change")
    changes: List[Change]
)

given JsonDecoder[Change]    = DeriveJsonDecoder.gen[Change]
given JsonDecoder[ChangeLog] = DeriveJsonDecoder.gen[ChangeLog]
