## Wal2Json for Scala
This application showcases ZIO & `ZStream` to continously read the Changelog in a [polling manner](https://www.postgresql.org/docs/current/logicaldecoding-example.html) from Postgres.
For this to work your Postgres needs to have the [wal2-json](https://github.com/eulerto/wal2json) plugin installed (see `Dockerfile`).

### Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`/`test`, and `sbt console` will start a Scala 3 REPL.
The tests will spin up the actual `docker compose` specification and execute the application in an end-to-end fashion.
For some more "testing" spin up the Postgres locally via `start.sh`, connect to it and insert some rows while having an active `sbt run` session in another window, to see how the rows will be consumed.


### Note
The combination of Scala 3 & ZIO 2 (still a release candidate) feels a bit clumsy here and there. I advise you to use VS Code & Metals instead of IntelliJ for this setup since there are some weird errors which IntelliJ displays, but are turn out to be no problem when doing `sbt compile` or using Metals.