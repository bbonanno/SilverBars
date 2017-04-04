package model

import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable

class LiveOrderBoardTest extends WordSpec with Matchers {

  trait Setup {
    val orderRepository = mutable.Map[OrderId, Order]()

    val liveOrderBoard = new LiveOrderBoard(orderRepository)
  }

  "registerOrder" should {

    "register an order in the board" in new Setup {
      val userId = "some id"
      val quantity = BigDecimal("3.5")
      val price = BigDecimal(303)
      val orderType = Buy

      val order = liveOrderBoard.registerOrder(userId, quantity, price, orderType)

      orderRepository should contain only (order.id -> order)

      order.userId shouldBe userId
      order.quantity shouldBe quantity
      order.price shouldBe price
      order.orderType shouldBe orderType
    }

  }

  "cancelOrder" should {

    "remove an order from the board" in new Setup {
      val order = liveOrderBoard.registerOrder("userId", BigDecimal("3.5"), BigDecimal(303), Buy)
      orderRepository should contain only (order.id -> order)

      val result = liveOrderBoard.cancelOrder(order.id)

      orderRepository shouldBe empty
    }
  }

  "orderSummary" should {

    "group orders with the same price" in new Setup {
      liveOrderBoard.registerOrder("user1", BigDecimal(3.5), BigDecimal("306"), Sell)
      liveOrderBoard.registerOrder("user2", BigDecimal(1.2), BigDecimal("310"), Sell)
      liveOrderBoard.registerOrder("user3", BigDecimal(1.5), BigDecimal("307"), Sell)
      liveOrderBoard.registerOrder("user4", BigDecimal(2), BigDecimal("306"), Sell)

      val summary = liveOrderBoard.orderSummary()

      summary.items should contain only (
        OrderSummaryItem(BigDecimal(5.5), BigDecimal("306"), Sell),
        OrderSummaryItem(BigDecimal(1.2), BigDecimal("310"), Sell),
        OrderSummaryItem(BigDecimal(1.5), BigDecimal("307"), Sell)
      )
    }

    "sort sell orders with the lowest price first" in new Setup {
      liveOrderBoard.registerOrder("user1", BigDecimal(3.5), BigDecimal("306"), Sell)
      liveOrderBoard.registerOrder("user2", BigDecimal(1.2), BigDecimal("310"), Sell)
      liveOrderBoard.registerOrder("user3", BigDecimal(1.5), BigDecimal("307"), Sell)

      val summary = liveOrderBoard.orderSummary()

      summary.items should contain inOrderOnly (
        OrderSummaryItem(BigDecimal(3.5), BigDecimal("306"), Sell),
        OrderSummaryItem(BigDecimal(1.5), BigDecimal("307"), Sell),
        OrderSummaryItem(BigDecimal(1.2), BigDecimal("310"), Sell)
      )
    }

    "sort buy orders with the highest price first" in new Setup {
      liveOrderBoard.registerOrder("user1", BigDecimal(3.5), BigDecimal("306"), Buy)
      liveOrderBoard.registerOrder("user2", BigDecimal(1.2), BigDecimal("310"), Buy)
      liveOrderBoard.registerOrder("user3", BigDecimal(1.5), BigDecimal("307"), Buy)

      val summary = liveOrderBoard.orderSummary()

      summary.items should contain inOrderOnly (
        OrderSummaryItem(BigDecimal(1.2), BigDecimal("310"), Buy),
        OrderSummaryItem(BigDecimal(1.5), BigDecimal("307"), Buy),
        OrderSummaryItem(BigDecimal(3.5), BigDecimal("306"), Buy)
      )
    }

    "produce the expected report" in new Setup {
      liveOrderBoard.registerOrder("user1", BigDecimal(3.5), BigDecimal("306"), Sell)
      liveOrderBoard.registerOrder("user2", BigDecimal(1.2), BigDecimal("310"), Sell)
      liveOrderBoard.registerOrder("user3", BigDecimal(1.5), BigDecimal("307"), Sell)
      liveOrderBoard.registerOrder("user4", BigDecimal(2), BigDecimal("306"), Sell)

      liveOrderBoard.registerOrder("user5", BigDecimal(4.5), BigDecimal("305"), Buy)
      liveOrderBoard.registerOrder("user6", BigDecimal(2.2), BigDecimal("309"), Buy)
      liveOrderBoard.registerOrder("user7", BigDecimal(1.5), BigDecimal("308"), Buy)
      liveOrderBoard.registerOrder("user8", BigDecimal(6), BigDecimal("305"), Buy)

      val summary = liveOrderBoard.orderSummary()

      summary.items should contain inOrderOnly (
        OrderSummaryItem(BigDecimal(5.5), BigDecimal("306"), Sell),
        OrderSummaryItem(BigDecimal(1.5), BigDecimal("307"), Sell),
        OrderSummaryItem(BigDecimal(1.2), BigDecimal("310"), Sell),
        OrderSummaryItem(BigDecimal(2.2), BigDecimal("309"), Buy),
        OrderSummaryItem(BigDecimal(1.5), BigDecimal("308"), Buy),
        OrderSummaryItem(BigDecimal(10.5), BigDecimal("305"), Buy)
      )
    }

  }

}
