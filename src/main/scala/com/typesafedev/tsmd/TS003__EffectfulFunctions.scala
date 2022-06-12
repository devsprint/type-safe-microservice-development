package com.typesafedev.tsmd

object TS003__EffectfulFunctions {

  class IO[+A](val unsafeInterpret: () => A) { s =>
    def map[B](f: A => B) = flatMap(f.andThen(IO.effect(_)))
    def flatMap[B](f: A => IO[B]): IO[B] =
      IO.effect(f(s.unsafeInterpret()).unsafeInterpret())
  }
  object IO {
    def effect[A](eff: => A) = new IO(() => eff)
  }

  def putStrLn(line: String): IO[Unit] =
    IO.effect(println(line))

  val getStrLn: IO[String] =
    IO.effect(scala.io.StdIn.readLine())

  val program: IO[String] =
    for {
      _    <- putStrLn("Hello, what's your name?")
      name <- getStrLn
      _    <- putStrLn(s"Hi, $name")
    } yield name

  def main(args: Array[String]): Unit = {
    program.unsafeInterpret()
  }
}
