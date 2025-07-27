package controllers.api

import controllers.base.JsonController
import models.MerchType
import play.api.libs.json.*
import play.api.mvc.*
import services.MerchTypeService

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MerchTypeController @Inject() (
    val controllerComponents: ControllerComponents,
    merchTypeService: MerchTypeService
)(implicit ec: ExecutionContext)
    extends BaseController
    with JsonController {

  def createMerchType(): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body
      .validate[MerchType]
      .fold(
        errors =>
          Future.successful(
            jsonFormatError(JsError.toJson(errors).toString)
          ),
        merchType =>
          merchTypeService.createMerchType(merchType).map { createdMerchType =>
            created(Json.toJson(createdMerchType))
          }
      )
  }

  def getMerchType(id: Int): Action[AnyContent] = Action.async {
    merchTypeService.getMerchType(id).map {
      case Some(merchType) => successWithMessage(Json.toJson(merchType))
      case None            => notFoundError(s"MerchType with ID $id")
    }
  }

  def getAllMerchTypes: Action[AnyContent] = Action.async {
    merchTypeService.getAllMerchTypes.map { merchTypes =>
      successWithMessage(Json.toJson(merchTypes))
    }
  }

  def updateMerchType(id: Int): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body
      .validate[MerchType]
      .fold(
        errors =>
          Future.successful(
            jsonFormatError(JsError.toJson(errors).toString)
          ),
        merchTypeUpdate =>
          merchTypeService.updateMerchType(merchTypeUpdate.copy(id = Some(id))).map {
            case Some(updatedMerchType) => created(Json.toJson(updatedMerchType))
            case None                   => notFoundError(s"MerchType ID $id tidak ada perubahan atau")
          }
      )
  }

  def deleteMerchType(id: Int): Action[AnyContent] = Action.async {
    merchTypeService.deleteMerchType(id).map { affectedRows =>
      if (affectedRows) successMessage(s"MerchType with ID $id deleted successfully")
      else notFoundError(s"MerchType with ID $id")
    }
  }
}
