val scala3Version  = "3.1.0"
val ZioJsonVersion = "0.3.0-RC1-1"

lazy val root = project
  .in(file("."))
  .settings(
    name                                 := "scala-wal2json",
    version                              := "0.1.0-SNAPSHOT",
    scalaVersion                         := scala3Version,
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.30.0",
    libraryDependencies += "dev.zio" %% "zio-json"     % ZioJsonVersion,
    libraryDependencies += "dev.zio" %% "zio"          % "2.0.0-RC2",
    libraryDependencies += "dev.zio" %% "zio-streams"  % "2.0.0-RC2",
    libraryDependencies += "dev.zio" %% "zio-test"     % "2.0.0-RC2",
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % "2.0.0-RC2",
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    libraryDependencies += "dev.zio" %% "zio-logging"       % "2.0.0-RC2",
    libraryDependencies += "dev.zio" %% "zio-logging-slf4j" % "2.0.0-RC2",
    libraryDependencies += "io.getquill" %% "quill-jdbc-zio" % "3.17.0.Beta3.0-RC2",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10",
    libraryDependencies += "org.postgresql" % "postgresql"      % "42.3.1",
    libraryDependencies += "com.dimafeng" %% "testcontainers-scala-postgresql" % "0.39.12" % "it,test",
    libraryDependencies += "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.39.12" % "test"
  )

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")

inThisBuild(
  Seq(
    scalafmtOnCompile := true
  )
)
