package model

import javax.inject.{Inject, Named}

import scala.collection.mutable

class LiveOrderBoard @Inject()(@Named("orderRepository") private val orderRepository: mutable.Map[OrderId, Order]) {

  def registerOrder(userId: String, quantity: BigDecimal, price: BigDecimal, orderType: OrderType): Order = {
    val order = Order(userId, quantity, price, orderType)
    orderRepository.put(order.id, order)
    order
  }

  def cancelOrder(id: OrderId): Unit = orderRepository.remove(id)

  def orderSummary(): OrderSummary = {

    def reportFor(orderType: OrderType, orders: Iterable[Order]) = {
      val ordering = orderType match {
        case Buy => Ordering[BigDecimal].reverse
        case Sell => Ordering[BigDecimal]
      }

      orders
        .groupBy(_.price)
        .map(t => OrderSummaryItem(t._2.map(_.quantity).sum, t._1, orderType))
        .toSeq
        .sortBy(_.price)(ordering)
    }

    val (sellOrders, buyOrders) = orderRepository.values.partition(_.orderType == Sell)

    OrderSummary(reportFor(Sell, sellOrders) ++ reportFor(Buy, buyOrders))
  }

}

case class OrderSummary(items: Seq[OrderSummaryItem])

case class OrderSummaryItem(quantity: BigDecimal, price: BigDecimal, orderType: OrderType)