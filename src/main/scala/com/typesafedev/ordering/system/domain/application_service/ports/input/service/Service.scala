package com.typesafedev.ordering.system.domain.application_service.ports.input.service

import com.typesafedev.ordering.system.domain.application_service.dto.create.OrderCommands
import com.typesafedev.ordering.system.domain.application_service.dto.create.OrderCommands.{CreateOrderCommand, CreateOrderResponse, OrderAddress, OrderItem}
import com.typesafedev.ordering.system.domain.application_service.dto.track.TrackOrder
import com.typesafedev.ordering.system.domain.application_service.dto.track.TrackOrder.{TrackOrderQuery, TrackOrderResponse}
import com.typesafedev.ordering.system.domain.application_service.ports.input.service.Service.OrderServiceError.{CustomerNotFound, FailToSaveOrder, InvalidOrder, RestaurantNotFound}
import com.typesafedev.ordering.system.domain.application_service.ports.output.repository.{CustomerRepository, OrderRepository, RestaurantRepository}
import com.typesafedev.ordering.system.domain.core.Domain.AggregateRoot.{Customer, Order, Restaurant}
import com.typesafedev.ordering.system.domain.core.Domain.BaseEntity.Product
import com.typesafedev.ordering.system.domain.core.Domain.{BaseEntity, CustomerId, OrderDomainService, OrderId, OrderItemId, RestaurantId, StreetAddress, StreetAddressId}
import zio.ZIO

import java.util.UUID

object Service {

  sealed trait OrderServiceError
  object OrderServiceError {
    final case class CustomerNotFound(message: String) extends OrderServiceError
    final case class RestaurantNotFound(message: String) extends OrderServiceError
    final case class InvalidOrder(message: String) extends OrderServiceError
    final case class FailToSaveOrder(message: String) extends OrderServiceError
    // TODO
  }

  trait OrderApplicationService {
    def createOrder(command: CreateOrderCommand): ZIO[Any, OrderServiceError, CreateOrderResponse]
    def trackOrder(query: TrackOrderQuery): ZIO[Any, OrderServiceError, TrackOrderResponse]
  }

  final case class OrderApplicationServiceImpl(orderRepository: OrderRepository, customerRepository: CustomerRepository, restaurantRepository: RestaurantRepository) extends OrderApplicationService {
    override def createOrder(command: OrderCommands.CreateOrderCommand):  ZIO[Any, OrderServiceError, CreateOrderResponse] = {
      val orderId: OrderId = OrderId(UUID.randomUUID())
      val transformOrderItemToOrderItemEntity = commandOrderItemToOrderItemEntity(orderId)_
      checkCustomer(command.customerId) *>  checkRestaurant(command).flatMap { restaurant =>
        val order = Order(command.customerId, restaurant.restaurantId, deliveryAddress = orderAddressToStreetAddress(command.address),
          price = command.price,
          orderItems = command.items.zipWithIndex.map {
            case (oi, index) => transformOrderItemToOrderItemEntity(oi).copy(orderItemId = OrderItemId(index))
          },
         orderId = orderId)
        OrderDomainService.validateAndInitiateOrder(order, restaurant) match {
          case Left(value) => ZIO.fail(InvalidOrder(value.map(_.message).mkString("\n")))
          case Right(value) => orderRepository.save(value.order).mapError(err => FailToSaveOrder(err.getMessage))
        }

      }.map { order =>
        CreateOrderResponse(
          order.trackingId,
          order.orderStatus,
          message = "Success"
        )

      }

    }

    override def trackOrder(query: TrackOrder.TrackOrderQuery): ZIO[Any, OrderServiceError, TrackOrderResponse] = ???

    private def checkCustomer(customerId: CustomerId): ZIO[Any, OrderServiceError,  Customer] = {
      customerRepository
        .findCustomer(customerId)
        .mapError(err => CustomerNotFound(s"Details: ${err.getMessage}"))
        .flatMap(op => ZIO.fromOption(op).orElseFail(CustomerNotFound(s"There is no customer in the system wth id $customerId.")))
    }

    private def checkRestaurant(command: OrderCommands.CreateOrderCommand): ZIO[Any, OrderServiceError, Restaurant] = {
        val restaurant = Restaurant(command.restaurantId, command.items.map(orderItem => Product(orderItem.productId)), active = true )
         restaurantRepository.findRestaurantInformation(restaurant)
           .mapError(err => RestaurantNotFound(s"Details: ${err.getMessage}"))
           .flatMap(op => ZIO.fromOption(op).orElseFail(RestaurantNotFound(s"There is no restaurant in the system wth id ${command.restaurantId}.")))
    }

    private def orderAddressToStreetAddress(orderAddress: OrderAddress):  StreetAddress = {
      StreetAddress(
        id = StreetAddressId(UUID.randomUUID()),
        street =  orderAddress.street,
        postalCode = orderAddress.postalCode,
        city = orderAddress.city
      )
    }

    private def commandOrderItemToOrderItemEntity(orderId: OrderId)(orderItem: OrderItem): BaseEntity.OrderItem = {
      BaseEntity.OrderItem(
        orderItemId = OrderItemId(0),
        orderId = orderId,
        product = Product(orderItem.productId),
        quantity = orderItem.quantity,
        price = orderItem.price,
        subTotal = orderItem.subTotal
      )
    }
  }
}
