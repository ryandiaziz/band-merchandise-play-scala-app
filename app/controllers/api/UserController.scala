package controllers.api

import controllers.base.JsonController
import models.User
import play.api.libs.json.*
import play.api.mvc.*
import services.UserService

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject() (
    val controllerComponents: ControllerComponents,
    userService: UserService
)(implicit ec: ExecutionContext)
    extends BaseController
    with JsonController {

  def createUser(): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body
      .validate[User.UserRequest]
      .fold(
        errors =>
          Future.successful(
            jsonFormatError(JsError.toJson(errors).toString)
          ),
        userRequest =>
          userService.createUser(userRequest).map { createdUser =>
            created(Json.toJson(createdUser))
          }
      )
  }

  def getUser(id: Int): Action[AnyContent] = Action.async {
    userService.getUser(id).map {
      case Some(user) => successWithMessage(Json.toJson(user))
      case None       => notFoundError(s"User ID $id")
    }
  }

  def getAllUsers: Action[AnyContent] = Action.async {
    userService.getAllUser.map { users =>
      successWithMessage(Json.toJson(users))
    }
  }

  def updateUser(id: Int): Action[JsValue] = Action(parse.json).async { implicit request =>
    request.body
      .validate[User]
      .fold(
        errors =>
          Future.successful(
            jsonFormatError(JsError.toJson(errors).toString)
          ),
        userUpdate =>
          userService.updateUser(userUpdate.copy(id = id)).map {
            case Some(updatedUser) => created(Json.toJson(updatedUser))
            case None              => notFoundError(s"User ID $id tidak ada perubahan atau")
          }
      )
  }

  def deleteUser(id: Int): Action[AnyContent] = Action.async {
    userService.deleteUser(id).map { affectedRows =>
      if (affectedRows) successMessage(s"User ID $id deleted successfully")
      else notFoundError(s"User ID $id")
    }
  }
}
