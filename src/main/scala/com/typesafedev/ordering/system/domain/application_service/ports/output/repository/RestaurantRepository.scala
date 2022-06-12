package com.typesafedev.ordering.system.domain.application_service.ports.output.repository

import com.typesafedev.ordering.system.domain.core.Domain.AggregateRoot.Restaurant
import zio.ZIO

trait RestaurantRepository {

  def findRestaurantInformation(restaurant: Restaurant): ZIO[Any, Throwable, Option[Restaurant]]



}
