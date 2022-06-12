package com.typesafedev.ordering.system.domain.application_service.ports.output.repository

import com.typesafedev.ordering.system.domain.core.Domain.AggregateRoot.Customer
import com.typesafedev.ordering.system.domain.core.Domain.CustomerId
import zio.ZIO

trait CustomerRepository {

  def findCustomer(customerId: CustomerId): ZIO[Any, Throwable, Option[Customer]]

}
