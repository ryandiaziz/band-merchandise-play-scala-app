package controllers.api

import controllers.base.JsonController
import models.barang.Barang
import play.api.libs.json.*
import play.api.mvc.*

import javax.inject.*
import repositories.*
import utils.LoggerUtil

@Singleton
class BarangController @Inject() (cc: ControllerComponents, barangRepo: BarangRepository)
    extends AbstractController(cc)
    with JsonController {

  private val logger: LoggerUtil.AppLogger = LoggerUtil.getLogger("Barang")

  def list(page: Int): Action[AnyContent] = Action {
    successWithMessage("Berhasil mendapatkan list barang", Json.toJson(barangRepo.all(page)))
  }

  def create: Action[AnyContent] = Action { request =>
    request.body.asJson match {
      case Some(json) => {
        try {
          // >>>>>>>>>>>>>> alternatif jika memiliki key yang beda <<<<<<<<<<<<<<
          val barang: Barang = Barang(
            nama = (json \ "nama").as[String],
            kategoriId = (json \ "kategori_id").as[Int],
            harga = (json \ "harga").asOpt[Double],
            stok = (json \ "stok").asOpt[Long]
          )

          barangRepo.create(barang) match {
            case Some(value) => successWithMessage("Berhasil menambahkan data barang", Json.obj("id" -> value))
            case None        => dbError()
          }
        } catch {
          case e: JsResultException => {
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
      }
      case None => {
        jsonExpected()
      }
    }
  }

  def detail(id: Int): Action[AnyContent] = Action {
    barangRepo
      .findByIdDetail(id)
      .map(b => successWithMessage("Berhasil", Json.toJson(b)))
      .getOrElse(NotFound("Not found"))
  }

  def update(): Action[AnyContent] = Action { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        try {
          val barang = Barang(
            id = (json \ "id").as[Int],
            nama = (json \ "nama").as[String],
            kategoriId = (json \ "kategori_id").as[Int],
            harga = (json \ "harga").asOpt[Double],
            stok = (json \ "stok").asOpt[Long]
          )

          barangRepo.update(barang) match {
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
    barangRepo.softDelete(id)
    Ok("Soft Deleted")
  }
}
