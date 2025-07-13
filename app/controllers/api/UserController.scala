package controllers.api

import controllers.base.JsonController
import models.user.User
import play.api.libs.json.*
import play.api.mvc.*

import javax.inject.*
import repositories.*
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import java.io.File
import javax.inject.Inject
import kong.unirest.Unirest

class UserController @Inject() (cc: ControllerComponents, userRepo: UserRepository)
    extends AbstractController(cc)
    with JsonController {

  def create: Action[AnyContent] = Action { request =>
    val formData = request.body.asMultipartFormData.getOrElse(throw Exception("form wajib"))

    try {
      val username = formData.dataParts
        .get("username")
        .flatMap(_.headOption)
        .getOrElse(throw Exception("username is required"))

      val photo = formData.file("photo").orNull

      if photo != null then {
        val file = new File(s"public/images/${photo.filename}")
        photo.ref.copyTo(file.toPath, replace = true)
      }

      val user = User(
        username = username,
        photo = if (photo != null) Some(photo.filename) else null
      )

      val res = userRepo.create(user)

      successWithMessage("Berhasil", Json.obj("username" -> username, "photo" -> photo.filename))
    } catch {
      case e: Exception => BadRequest(Json.obj("message" -> e.getMessage))
    }
  }

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def servePhoto(id: Long): Action[AnyContent] = Action { request =>
    try {
      val user = userRepo.getUser(id)._1.getOrElse(throw Exception("something went wrong"))

      val photo = user.photo.getOrElse(throw Exception("photo not found"))
      val file  = new File(s"public/images/$photo")

      if (file.exists()) {
        Ok.sendFile(
          content = file,
          fileName = _ => Some(photo)
        ).as("image/png")
      } else {
        BadRequest(Json.obj("message" -> "file not found"))
      }
    } catch {
      case e: Exception => BadRequest(Json.obj("message" -> e.getMessage))
    }
  }

  def unirest(): Action[AnyContent] = Action { request =>
    val body = Json.obj(
      "model"  -> "qwen3:0.6b",
      "prompt" -> "who is indonesian presidents",
      "format" -> "json",
      "stream" -> false
    )

    val response = Unirest
      .post("http://192.168.101.99:11434/api/generate")
      .header("Content-Type", "application/json")
      .body(body.toString)
      .asString()

    val jsonParse     = Json.parse(response.getBody)
    val responseParse = (jsonParse \ "response").as[String]

    Ok(Json.obj("response" -> responseParse))
  }
}
