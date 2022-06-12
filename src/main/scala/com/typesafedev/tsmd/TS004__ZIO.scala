package com.typesafedev.tsmd

import zio._

import scala.concurrent.Future
import scala.io.StdIn
import scala.language.postfixOps

object TS004__ZIO  extends ZIOAppDefault {

  // ZIO[-R, +E, +A]

  val s1 = ZIO.succeed(42)

  val failure = ZIO.fail("Boom !")

  val option: IO[Option[Nothing], Int] = ZIO.fromOption(Some(2))

  option.mapError(_ => "Missing value")

  lazy val future = Future.successful("Hello !")
  val zf = ZIO.fromFuture { implicit ec =>
    future.map(_ => "Good Bye !")
  }

  // Side effects

  // Synchronous side effects
  val readLine = ZIO.attempt(StdIn.readLine())
  def printLine(line: String) = ZIO.attempt(println(line))


   val program =  for {
      _    <- printLine("Hello, what's your name?")
      name <- readLine
      _    <- printLine(s"Hi, $name")
    } yield name

  // Asynchronous side effect
  trait User
  trait AuthError
  object legacy_callback {

    def login(onSuccess: User => Unit, onFailure: AuthError => Unit): Unit =  ???


  }

  val login: IO[AuthError, User] = ZIO.async { callback =>
    legacy_callback.login(
      user => callback(ZIO.succeed(user)),
      err => callback(ZIO.fail(err))
    )
  }

  // blocking synchronous effect

  val sleeping = ZIO.attemptBlocking(Thread.sleep(1000))

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = program.exitCode
}
