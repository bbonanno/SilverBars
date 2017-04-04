package controllers

import javax.inject._

import model._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._

import scala.util.{Failure, Success, Try}

case class RegisterOrderRequest(userId: String, quantity: BigDecimal, price: BigDecimal, orderType: OrderType)

object RegisterOrderRequest {

  val validOrderTypes = Set("Sell", "Buy")
  def validValuesValidator = Reads.StringReads.filter(ValidationError("Invalid order type"))(validOrderTypes.contains)

  implicit val reads: Reads[RegisterOrderRequest] = (
    (JsPath \ "userId").read[String] and
      (JsPath \ "quantity").read[BigDecimal] and
      (JsPath \ "price").read[BigDecimal] and
      (JsPath \ "orderType").read[String](validValuesValidator).map{
        case "Buy" => Buy
        case "Sell" => Sell
      }
    )(RegisterOrderRequest.apply _)
}

@Singleton
class OrderController @Inject()(val liveOrderBoard: LiveOrderBoard) extends Controller {

  //Ideally we would have async actions, but as all our code is synchronous and I don't wanna over complicate the solution just for showing off, I'll let it sync
  def registerOrder = Action(parse.json) { implicit request =>
    withJsonBody[RegisterOrderRequest] { registerOrderRequest =>
      val order = liveOrderBoard.registerOrder(
        userId = registerOrderRequest.userId,
        price = registerOrderRequest.price,
        quantity = registerOrderRequest.quantity,
        orderType = registerOrderRequest.orderType
      )
      Created.withHeaders(LOCATION -> routes.OrderController.cancelOrder(order.id.id).url)
    }
  }

  def cancelOrder(id: String) = Action { implicit request =>
    liveOrderBoard.cancelOrder(OrderId(id))
    NoContent
  }

  implicit val orderTypeWrites = new Writes[OrderType] {
    override def writes(o: OrderType): JsValue = o match {
      case Buy => JsString("Buy")
      case Sell => JsString("Sell")
    }
  }
  implicit val orderSummaryItemWrites = Json.writes[OrderSummaryItem]
  implicit val orderSummaryWrites = Json.writes[OrderSummary]

  def orderSummary() = Action { implicit request =>
    val orderSummary = liveOrderBoard.orderSummary()

    Ok(Json.toJson(orderSummary))
  }

  private def withJsonBody[T](f: (T) => Result)(implicit request: Request[JsValue], m: Manifest[T], reads: Reads[T]): Result =
    Try(request.body.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(JsError(errs)) => BadRequest(s"Invalid ${m.runtimeClass.getSimpleName} payload: $errs")
      case Failure(e) => BadRequest(s"could not parse body due to ${e.getMessage}")
    }
}
