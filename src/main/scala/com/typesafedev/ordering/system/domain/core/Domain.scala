package com.typesafedev.ordering.system.domain.core

import com.typesafedev.ordering.system.domain.core.Domain.BaseEntity.OrderItem
import com.typesafedev.ordering.system.domain.core.Domain.DomainError.{OrderPriceCheckingFailed, RestaurantIsNotActive}
import com.typesafedev.ordering.system.domain.core.events.Events.DomainEvent.{OrderCancelledEvent, OrderCreatedEvent, OrderPaidEvent}
import zio.prelude.{Newtype, Validation}

import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID
import scala.math.BigDecimal.RoundingMode

object Domain {

  // value objects
  object OrderId extends Newtype[UUID]
  type OrderId = OrderId.Type

  object CustomerId extends Newtype[UUID]
  type CustomerId = CustomerId.Type

  object RestaurantId extends Newtype[UUID]
  type RestaurantId = RestaurantId.Type

  object StreetAddressId extends Newtype[UUID]
  type StreetAddressId = StreetAddressId.Type

  object OrderItemId extends Newtype[Long]
  type OrderItemId = OrderItemId.Type

  object TrackingId extends Newtype[UUID]
  type TrackingId = TrackingId.Type

  object ProductId extends Newtype[UUID]
  type ProductId = ProductId.Type

  final case class StreetAddress(id: StreetAddressId, street: String, postalCode: String, city: String)

  final case class Money(amount: BigDecimal) { self =>
    def isGreaterThanZero: Boolean =  amount > BigDecimal.valueOf(0.0)
    def isGreaterThan(other: Money): Boolean = self.amount > other.amount
    def > (other: Money): Boolean = isGreaterThan(other)
    def + (other: Money): Money = Money(setScale(self.amount + other.amount))
    def - (other: Money): Money = Money(setScale(self.amount - other.amount))
    def * (multiplier: Int): Money = Money(setScale(self.amount * multiplier))
    def equalTo(other: Money): Boolean = self.amount.compareTo(other.amount) == 0

    private def setScale(input: BigDecimal): BigDecimal = input.setScale(2, RoundingMode.HALF_EVEN)
  }

  final val ZERO: Money = Money(BigDecimal(0.0))

  sealed trait PaymentStatus
  object PaymentStatus {
    final case object COMPLETED extends PaymentStatus
    final case object CANCELLED extends PaymentStatus
    final case object FAILED extends PaymentStatus
  }

  sealed trait OrderApprovalStatus
  object OrderApprovalStatus {
    final case object APPROVED extends OrderApprovalStatus
    final case object REJECTED extends OrderApprovalStatus
  }

  sealed trait OrderStatus
  object OrderStatus {
    final case object PENDING extends OrderStatus
    final case object PAID extends OrderStatus
    final case object APPROVED extends OrderStatus
    final case object CANCELLING extends OrderStatus
    final case object CANCELLED extends OrderStatus
  }

  sealed trait DomainError {
    val message: String
  }
  object DomainError {
    final case class OrderTotalPriceCheckingFailed(message: String) extends DomainError
    final case class OrderPriceCheckingFailed(message: String) extends DomainError
    final case class RestaurantIsNotActive(message: String) extends DomainError
  }

  sealed trait BaseEntity
  object BaseEntity {
    final case class OrderItem(orderItemId: OrderItemId, orderId: OrderId, product: Product, quantity: Int, price: Money, subTotal: Money) extends BaseEntity
    final case class Product(productId: ProductId, name: String ="", price: Money = ZERO) extends BaseEntity
  }

  sealed trait AggregateRoot extends BaseEntity

  object AggregateRoot  {
    final case class Order(
                            customerId: CustomerId,
                            restaurantId: RestaurantId,
                            deliveryAddress: StreetAddress,
                            orderId: OrderId = OrderId(UUID.randomUUID()),
                            price: Money = ZERO,
                            orderItems: List[OrderItem] = List.empty,
                            trackingId: TrackingId = TrackingId(UUID.randomUUID()),
                            orderStatus: OrderStatus = OrderStatus.PENDING,
                            failures: List[Error] = List.empty
                          ) extends AggregateRoot

    final case class Customer(customerId: CustomerId)

    final case class Restaurant(restaurantId: RestaurantId, products: List[BaseEntity.Product], active: Boolean)
  }


  import AggregateRoot._



  sealed trait OrderValidation { self =>
    import OrderValidation._

    def <+> (other: OrderValidation): OrderValidation = combine(other)
    private def combine(that: OrderValidation): OrderValidation = Combine(self, that)

    def apply(order: Order): Either[List[DomainError], Order] = OrderValidation.isOrderValid(order)

  }

  object OrderValidation {
    final case class Combine(left: OrderValidation, right: OrderValidation) extends OrderValidation
    case object OrderItemPriceValidation extends OrderValidation
    case object OrderPriceIsPositiveValidation extends OrderValidation
    case object OrderPriceConsistencyValidation extends OrderValidation


    def validateOrderItemPrice: OrderValidation = OrderItemPriceValidation
    def validateOrderPriceIsGreaterThanZero: OrderValidation = OrderPriceIsPositiveValidation
    def validateOrderPriceConsistency: OrderValidation = OrderPriceConsistencyValidation

    def isOrderValid( order: Order, validation: OrderValidation = default): Either[List[DomainError], Order] = validation match {
      case Combine(left, right) => (isOrderValid(order, left), isOrderValid(order, right)) match {
        case (Right(l), Right(_)) => Right(l)
        case (Left(l), Right(_)) => Left(l)
        case (Right(_), Left(r)) => Left(r)
        case (Left(l), Left(r)) => Left(l ++ r)
      }
      case OrderItemPriceValidation =>
       order.orderItems.map(validateOrderItemPriceInternal).foldLeft[Either[List[DomainError], Order]](Right(order)) { (acc, b) =>
         (acc, b) match {
           case (Right(l), Right(_)) => Right(l)
           case (Left(l), Right(_)) => Left(l)
           case (Right(_), Left(r)) => Left(r)
           case (Left(l), Left(r)) => Left(l ++ r)
         }
       }


      case OrderPriceIsPositiveValidation => validatePrice(order)

      case OrderPriceConsistencyValidation => validatePriceConsistency(order)
    }


    private def validateOrderItemPriceInternal: OrderItem => Either[List[DomainError], OrderItem] = { orderItem =>
      if (orderItem.price.isGreaterThanZero && orderItem.product.price.equalTo(orderItem.price) && orderItem.subTotal.equalTo(orderItem.price * orderItem.quantity))
        Right(orderItem)
      else
        Left(List(OrderPriceCheckingFailed(s"Order Item (${orderItem.orderId} price (${orderItem.price}) is not valid.")))
    }

    private def validatePrice(order: Order): Either[List[DomainError], Order] =
      if (order.price.isGreaterThanZero && order.orderItems.map(_.price).forall(_.isGreaterThanZero))
        Right(order)
      else
        Left(List(OrderPriceCheckingFailed("Order price or order items price are not greater than zero")))

    private def validatePriceConsistency(order: Order): Either[List[DomainError], Order] = {
      val total = order.orderItems.map(_.subTotal).foldLeft(ZERO)((acc,b) => acc +b)
      if (total.equalTo(order.price))
        Right(order)
      else
        Left(List(DomainError.OrderTotalPriceCheckingFailed(s"The order price (${order.price.amount}) is different from the sum of order item sub total (${total.amount}).")))
    }


    val default: OrderValidation =   validateOrderPriceIsGreaterThanZero <+> validateOrderItemPrice <+> validateOrderPriceConsistency
  }


  object OrderDomainService {

    def validateAndInitiateOrder(order: Order, restaurant: Restaurant): Either[List[DomainError],  OrderCreatedEvent] = for {
      _ <- validateRestaurant(restaurant)
      updatedOrder = setProductInformation(order, restaurant)
      _ <- OrderValidation.isOrderValid(updatedOrder)
    } yield OrderCreatedEvent(updatedOrder, ZonedDateTime.now(ZoneId.of("UTC")))

    def payOrder(order: Order): Either[List[DomainError], OrderPaidEvent] =  ???
    def approveOrder(order: Order): Either[List[DomainError], Order] =  ???
    def cancelOrderPayment(order: Order, failures: List[DomainError]): Either[List[DomainError], OrderCancelledEvent] = ???
    def cancelOrder(order: Order, failures: List[DomainError]): Either[List[DomainError], Order] = ???


    private def validateRestaurant(restaurant: Restaurant): Either[List[DomainError], Restaurant] = if (restaurant.active)
      Right(restaurant)
    else
      Left(List(RestaurantIsNotActive(s"Restaurant ${restaurant.restaurantId} is not active.")))

    private def setProductInformation(order: Order, restaurant: Restaurant): Order = {
      val products = restaurant.products.map { p =>
        p.productId -> p
      }.toMap

      order.copy(orderItems =
        order.orderItems.flatMap { orderItem =>
          products.get(orderItem.product.productId) match {
            case Some(p) => List(orderItem.copy(product = orderItem.product.copy(name = p.name, price = p.price)))
            case None => List.empty[OrderItem]
          }
        }
      )
    }


  }

}
