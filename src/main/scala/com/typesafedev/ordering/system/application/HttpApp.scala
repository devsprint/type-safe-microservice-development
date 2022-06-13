package com.typesafedev.ordering.system.application

import com.typesafedev.ordering.system.application.JsonSupport._
import com.typesafedev.ordering.system.domain.application_service.dto.create.OrderCommands.CreateOrderCommand
import com.typesafedev.ordering.system.domain.application_service.dto.track.TrackOrder.TrackOrderQuery
import com.typesafedev.ordering.system.domain.application_service.ports.input.service.Service.OrderApplicationService
import com.typesafedev.ordering.system.domain.core.Domain.TrackingId
import zhttp.http.Method.{GET, POST}
import zhttp.http._
import zio.json.{DecoderOps, EncoderOps}
import zio.{Task, ZIO}

import java.util.UUID

object HttpApp {

  val app = Http.collectZIO[Request] {
    case req @ POST -> !! / "orders" =>
      val orderCommand: Task[CreateOrderCommand] =
        req.bodyAsString.flatMap(raw => ZIO.fromEither(raw.fromJson[CreateOrderCommand])
          .mapError(err => new RuntimeException(err)))

       for {
        orderCmd <- orderCommand
        response <-  OrderApplicationService.createOrder(orderCmd).mapError(err => new RuntimeException(err.message))

      } yield Response.json(response.toJsonPretty)

    case GET -> !! / "orders" / trackingId  =>
      OrderApplicationService.trackOrder(TrackOrderQuery(TrackingId(UUID.fromString(trackingId))))
        .mapBoth(err => new RuntimeException(err.message), resp => Response.json(resp.toJsonPretty))

  }

}
