package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

class OrderControllerSpec extends PlaySpec with GuiceOneAppPerTest {

  "POST /orders" should {

    "fail for an invalid json" in {
      val body = Json.obj()

      val request = FakeRequest(POST, "/orders").withBody(body)
      val response = route(app, request).get

      contentAsString(response) must include("Invalid RegisterOrderRequest payload")
      status(response) mustBe BAD_REQUEST
    }

    "fail for an invalid order type" in {
      val body = Json.obj("userId" -> "user1", "quantity" -> 3.5, "price" -> 306, "orderType" -> "fmdkmfk")

      val request = FakeRequest(POST, "/orders").withBody(body)
      val response = route(app, request).get

      contentAsString(response) must include("Invalid order type")
      status(response) mustBe BAD_REQUEST
    }

    "return the location of the created order" in {
      val body = Json.obj("userId" -> "user1", "quantity" -> 3.5, "price" -> 306, "orderType" -> "Sell")

      val request = FakeRequest(POST, "/orders").withBody(body)
      val response = route(app, request).get

      contentAsString(response) mustBe empty

      status(response) mustBe CREATED
      header(LOCATION, response).get must fullyMatch regex "^/orders/.*"
    }
  }

  "GET /orders/summary" should {

    "return the orders summary" in {
      route(app, FakeRequest(POST, "/orders").withBody(Json.obj("userId" -> "user1", "quantity" -> 3.5, "price" -> 306, "orderType" -> "Sell"))).get

      val request = FakeRequest(GET, "/orders/summary")
      val response = route(app, request).get

      status(response) mustBe OK
      contentAsJson(response) mustBe Json.obj("items" -> Json.arr(
        Json.obj("quantity" -> 3.5, "price" -> 306, "orderType" -> "Sell")
      ))
    }

  }

  "DELETE /orders/:id" should {

    "cancel an existing order" in {
      val creation = route(app, FakeRequest(POST, "/orders").withBody(Json.obj("userId" -> "user1", "quantity" -> 3.5, "price" -> 306, "orderType" -> "Sell"))).get
      val orderUrl = header(LOCATION, creation).get

      val response = route(app, FakeRequest(DELETE, orderUrl)).get

      contentAsString(response) mustBe empty
      status(response) mustBe NO_CONTENT

      val summary = route(app, FakeRequest(GET, "/orders/summary")).get
      status(summary) mustBe OK
      contentAsJson(summary) mustBe Json.obj("items" -> Json.arr())
    }

  }
}
