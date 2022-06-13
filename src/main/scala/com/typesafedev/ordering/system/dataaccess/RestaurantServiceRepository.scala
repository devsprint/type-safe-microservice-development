package com.typesafedev.ordering.system.dataaccess

import com.typesafedev.ordering.system.domain.application_service.ports.output.repository.Repositories.RestaurantRepository
import com.typesafedev.ordering.system.domain.core.Domain.AggregateRoot
import zio.{ZIO, ZLayer}

object RestaurantServiceRepository {

  object RestaurantRepository {
    val live = ZLayer.succeed(RestaurantRepositoryLive())
  }

  final case class RestaurantRepositoryLive() extends RestaurantRepository {
    override def findRestaurantInformation(restaurant: AggregateRoot.Restaurant): ZIO[Any, Throwable, Option[AggregateRoot.Restaurant]] = ???
  }

}
