package com.typesafedev.ordering.system.application

import com.typesafedev.ordering.system.dataaccess.CustomerServiceRepository.CustomerRepository
import com.typesafedev.ordering.system.dataaccess.OrderServiceRepository.OrderRepository
import com.typesafedev.ordering.system.dataaccess.RestaurantServiceRepository.RestaurantRepository
import com.typesafedev.ordering.system.domain.application_service.ports.input.service.Service.OrderApplicationService
import com.typesafedev.ordering.system.messaging.OrdersPublisher.OrdersCreatedPublisher
import zhttp.http.middleware.HttpMiddleware
import zhttp.http.{Middleware, Patch}
import zhttp.service.Server
import zio._

import java.io.IOException
import java.util.concurrent.TimeUnit
import scala.language.postfixOps

object OrderServiceBoot extends ZIOAppDefault {

  val serverTime: HttpMiddleware[Clock, Nothing] = Middleware.patchZIO(_ =>
    for {
      currentMilliseconds <- Clock.currentTime(TimeUnit.MILLISECONDS)
      withHeader = Patch.addHeader("X-Time", currentMilliseconds.toString)
    } yield withHeader,
  )

  val middlewares: HttpMiddleware[Console with Clock, IOException] =
  // print debug info about request and response
    Middleware.debug ++
      // close connection if request takes more than 3 seconds
      Middleware.timeout(3 seconds) ++
      // add static header
      Middleware.addHeader("X-Environment", "Dev") ++
      // add dynamic header
      serverTime

  override def run =  Server.start(8090, HttpApp.app @@ middlewares)
    .provide(
      Console.live,
      Clock.live,
      OrderApplicationService.live,
      OrderRepository.live,
      CustomerRepository.live,
      RestaurantRepository.live,
      OrdersCreatedPublisher.live
    )

}
