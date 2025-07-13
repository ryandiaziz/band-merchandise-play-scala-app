package controllers.api

import controllers.base.JsonController
import models.kategori.Kategori
import play.api.libs.json.*
import play.api.mvc.*

import javax.inject.*
import repositories.*
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import repositories.KategoriRepository

import javax.inject.Inject

class KategoriController @Inject() (cc: ControllerComponents, kategoriRepo: KategoriRepository)
    extends AbstractController(cc)
    with JsonController {

  def list: Action[AnyContent] = Action {
    successWithMessage("Berhasil mendapatkan data", Json.toJson(kategoriRepo.all()))
  }

  def create: Action[AnyContent] = Action { request =>
    request.body.asJson match {
      case Some(json) => {
        val namaOpt = (json \ "nama").asOpt[String]

        namaOpt match {
          case Some(nama) if nama.nonEmpty => {
            val kategori = Kategori(-1, nama)

            kategoriRepo.create(kategori) match {
              case Some(id) => successWithMessage("Berhasil menambahkan", Json.obj("id" -> id))
              case None     => dbError()
            }
          }
          case _ => {
            jsonMissingField("nama")
          }
        }
      }
      case None => {
        jsonExpected()
      }
    }
  }

  def detail(id: Int): Action[AnyContent] = Action {
    kategoriRepo
      .findById(id)
      .map(k => successWithMessage("Berhasil mendapatkan data", Json.toJson(k)))
      .getOrElse(notFoundError("kategori"))
  }

  def update(): Action[AnyContent] = Action { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        try {
          val kategori = Kategori(
            id = (json \ "id").as[Int],
            nama = (json \ "nama").as[String]
          )

          println(kategori)

          kategoriRepo.update(kategori) match {
            case Some(updatedId) =>
              Ok(
                Json.obj(
                  "message" -> "alhamdulillah",
                  "data"    -> Json.obj("id" -> updatedId)
                )
              )
            case None =>
              BadRequest(Json.obj("message" -> "anak nakal!"))
          }

        } catch {
          case e: JsResultException => {
            println(e.getMessage)
            val errorDetails = e.errors.map { case (path, validationErrors) =>
              Json.obj(
                "field"  -> path.toString().stripPrefix("/"),
                "errors" -> validationErrors.map(_.message)
              )
            }

            BadRequest(
              Json.obj(
                "status"  -> "error",
                "message" -> "Gagal membaca data JSON",
                "detail"  -> errorDetails
              )
            )
          }
          case e: Exception => {
            BadRequest(
              Json.obj(
                "status"  -> "error",
                "message" -> "Gagal membaca data JSON",
                "detail"  -> e.getMessage
              )
            )
          }
        }

      case None =>
        BadRequest(
          Json.obj(
            "status"  -> "error",
            "message" -> "Body bukan JSON",
            "detail"  -> None
          )
        )
    }
  }

  def delete(id: Int): Action[AnyContent] = Action {
    kategoriRepo.softDelete(id)
    successMessage("Soft deleted")
  }
}
