package com.typesafedev.ordering.system.dataaccess

import com.typesafedev.ordering.system.domain.application_service.ports.output.repository.Repositories.CustomerRepository
import com.typesafedev.ordering.system.domain.core.Domain.{AggregateRoot, CustomerId}
import zio.{ZIO, ZLayer}

object CustomerServiceRepository {

  object CustomerRepository {

    val live = ZLayer.succeed(CustomerRepositoryLive())

  }

  final case class CustomerRepositoryLive() extends CustomerRepository {
    override def findCustomer(customerId: CustomerId): ZIO[Any, Throwable, Option[AggregateRoot.Customer]] = ???
  }

}
