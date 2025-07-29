package controllers.api

import controllers.base.JsonController
import models.{Cart, CartMerch}
import play.api.libs.json.*
import play.api.mvc.*
import services.CartService

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartController @Inject() (
    val controllerComponents: ControllerComponents,
    cartService: CartService
)(implicit ec: ExecutionContext)
    extends BaseController
    with JsonController {

  def addItemToCart() = Action(parse.json).async { implicit request: Request[JsValue] =>
    request.body
      .validate[CartMerch.AddItemToCartRequest]
      .fold(
        errors => {
          Future.successful(
            jsonFormatError(JsError.toJson(errors).toString)
          )
        },
        addRequest => {
          cartService.addItemToCart(addRequest).map {
            case Right(cart) =>
              created(Json.toJson(cart))
            case Left(errorMessage) =>
              notFoundError(errorMessage)
          }
        }
      )
  }

  def getCart(id: Int) = Action.async {
    cartService.getCart(id).map {
      case Some(cart) => successWithMessage(Json.toJson(cart))
      case None       => notFoundError(s"Cart with ID $id not found")
    }
  }

  def getUserActiveCart(userId: Int) = Action.async {
    cartService.getUserActiveCart(userId).map {
      case Some(cart) => successWithMessage(Json.toJson(cart))
      case None       => notFoundError(s"Active cart for User ID $userId not found")
    }
  }

  def softDeleteCart(id: Int) = Action.async {
    cartService.softDeleteCart(id).map { success =>
      if (success) successMessage(s"Cart with ID $id soft-deleted successfully")
      else notFoundError(s"Cart with ID $id not found")
    }
  }
}
