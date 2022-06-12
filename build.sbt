ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.typesafedev"
ThisBuild / organizationName := "Type Safe Dev SRL"

lazy val root = (project in file("."))
  .settings(
    name := "type-safe-microservice-development",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.0-RC6",
      "dev.zio" %% "zio-test" % "2.0.0-RC6" % Test,
      "dev.zio" %% "zio-prelude" % "1.0.0-RC14",
      "io.d11" % "zhttp_2.13" % "2.0.0-RC9",

    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
