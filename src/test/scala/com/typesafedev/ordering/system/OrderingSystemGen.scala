package com.typesafedev.ordering.system

import com.typesafedev.ordering.system.domain.core.Domain.AggregateRoot.{Customer, Order, Restaurant}
import com.typesafedev.ordering.system.domain.core.Domain.BaseEntity.Product
import com.typesafedev.ordering.system.domain.core.Domain.{CustomerId, Money, OrderId, ProductId, RestaurantId}
import zio.test.Gen

object OrderingSystemGen {


  val orderIdGen = Gen.uuid.map(u => OrderId(u))
  val restaurantIdGen = Gen.uuid.map(u => RestaurantId(u))
  val customerIdGen = Gen.uuid.map(u => CustomerId(u))
  val productIdGen = Gen.uuid.map(u => ProductId(u))

  val customerGen = customerIdGen.map(cid => Customer(cid))

  val moneyGen = Gen.bigDecimal(0.00, 500.00).map(v => Money(v))

  val productGen = for {
    pId <- productIdGen
    name <- Gen.asciiString
    price <- moneyGen
  } yield Product(pId, name, price)

  val productsGen = Gen.listOf(productGen)

  val restaurantGen = for {
    id <- restaurantIdGen
    products <- productsGen
    active <- Gen.boolean
  } yield Restaurant(id, products, active)

//  val orderGen = for {
//    customerId <- customerIdGen
//    id <- orderIdGen
//    restaurantId <- restaurantIdGen
//
//
//  } yield Order(customerId, restaurantId, ???, id, price )

}
