package model

import java.util.UUID

sealed trait OrderType

case object Buy extends OrderType

case object Sell extends OrderType

case class OrderId(id: String)

case class Order(userId: String, quantity: BigDecimal, price: BigDecimal, orderType: OrderType, id: OrderId = OrderId(UUID.randomUUID().toString))
