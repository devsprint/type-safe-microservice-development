package com.typesafedev.ordering.system.domain.application_service.ports.output.repository

import com.typesafedev.ordering.system.domain.core.Domain.AggregateRoot.{Customer, Order, Restaurant}
import com.typesafedev.ordering.system.domain.core.Domain.{CustomerId, TrackingId}
import zio.ZIO

object Repositories {

  trait OrderRepository {

    def save(order: Order): ZIO[Any,Throwable,Order]
    def findByTrackingId (trackingId: TrackingId): ZIO[Any, Throwable, Option[Order]]
  }

  trait CustomerRepository {

    def findCustomer(customerId: CustomerId): ZIO[Any, Throwable, Option[Customer]]

  }

  trait RestaurantRepository {
    def findRestaurantInformation(restaurant: Restaurant): ZIO[Any, Throwable, Option[Restaurant]]
  }

}
