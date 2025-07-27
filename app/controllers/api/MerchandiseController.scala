package controllers.api

import controllers.base.JsonController
import models.Merchandise
import play.api.libs.json.*
import play.api.mvc.*
import services.MerchandiseService

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MerchandiseController @Inject() (
    val controllerComponents: ControllerComponents,
    merchandiseService: MerchandiseService
)(implicit ec: ExecutionContext)
    extends BaseController
    with JsonController {

  def createMerchType(): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body
      .validate[Merchandise.MerchandiseRequest]
      .fold(
        errors =>
          Future.successful(
            jsonFormatError(JsError.toJson(errors).toString)
          ),
        merchType =>
          merchandiseService.createMerchandise(merchType).map {
            case Right(merchandise) => created(Json.toJson(merchandise))
            case Left(errorMessage) => notFoundError(errorMessage)
          }
      )
  }

  def getMerchType(id: Int): Action[AnyContent] = Action.async {
    merchandiseService.getMerchandise(id).map {
      case Some(merchType) => successWithMessage(Json.toJson(merchType))
      case None            => notFoundError(s"Merchandise ID $id")
    }
  }

  def getAllMerchTypes: Action[AnyContent] = Action.async {
    merchandiseService.getAllMerchandise.map { merchTypes =>
      successWithMessage(Json.toJson(merchTypes))
    }
  }

  def updateMerchType(id: Int): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body
      .validate[Merchandise.MerchandiseRequest]
      .fold(
        errors =>
          Future.successful(
            jsonFormatError(JsError.toJson(errors).toString)
          ),
        merchTypeUpdate =>
          merchandiseService.updateMerchType(id, merchTypeUpdate).map {
            case Some(updatedMerchType) => created(Json.toJson(updatedMerchType))
            case None                   => notFoundError(s"Merchandise $id tidak ada perubahan atau")
          }
      )
  }

  def deleteMerchType(id: Int): Action[AnyContent] = Action.async {
    merchandiseService.softDeleteMerchandise(id).map { affectedRows =>
      if (affectedRows) successMessage(s"Merchandise ID $id deleted successfully")
      else notFoundError(s"Merchandise ID $id")
    }
  }
}
