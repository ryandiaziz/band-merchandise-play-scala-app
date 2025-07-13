package controllers.api
import controllers.base.JsonController
import models.keranjang.{Keranjang, KeranjangAddItemRequest}
import play.api.libs.json.*
import play.api.mvc.*
import repositories.{BarangRepository, KeranjangRepository}
import services.KeranjangService

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class KeranjangController @Inject() (
    val controllerComponents: ControllerComponents,
    keranjangService: KeranjangService,
    keranjangRepo: KeranjangRepository,
    barangRepo: BarangRepository
)(implicit ec: ExecutionContext)
    extends BaseController
    with JsonController {

  def tambahItemKeKeranjang(): Action[JsValue] = Action(parse.json).async { implicit request: Request[JsValue] =>
    request.body
      .validate[KeranjangAddItemRequest]
      .fold(
        errors => {
          Future.successful(
            BadRequest(
              Json.obj("error" -> "Format request tidak valid.")
            )
          )
        },
        addRequest => {
          keranjangService.tambahItemKeKeranjang(addRequest).map {
            case Right(keranjang) =>
              Ok(Json.toJson(keranjang))
            case Left(errorMessage) =>
              BadRequest(Json.obj("error" -> errorMessage))
          }
        }
      )
  }

  def list(page: Int, size: Int): Action[AnyContent] = Action {
    Ok("ok")
//    val result = keranjangRepo.getAll()
//    successWithMessage("Berhasil mendapatkan data keranjang", Json.toJson(result))
  }

  def listByUserId(userId: Int): Action[AnyContent] = Action {
    Ok("ok")
//    val result = keranjangRepo.getByUserId(userId)
//    Ok(Json.toJson(result.get))
  }

  def get(id: Int): Action[AnyContent] = Action {
    Ok("ok")
//    keranjangRepo
//      .getById(id)
//      .map(k => Ok(Json.toJson(k)))
//      .getOrElse(NotFound(Json.obj("message" -> "Data tidak ditemukan")))
  }

  // Karena Anda menggunakan parse.json, request.body sekarang pasti JsValue
  def addItem(): Action[JsValue] = Action(parse.json) { implicit request: Request[JsValue] =>
    Ok("ok")
//    try {
//      val keranjangItemReq: KeranjangAddItemRequest = request.body.validate[KeranjangAddItemRequest] match {
//        case JsSuccess(item, path) => item
//        case JsError(errors)       => throw Exception(JsError.toJson(errors).toString)
//      }
//
//      println(keranjangItemReq)
//
//      barangRepo.findById(keranjangItemReq.barangId) match {
//        case Some(barangData) => {
//          val currentStock: Long = barangData.stok.getOrElse(0)
//
//          if (currentStock < keranjangItemReq.jumlah)
//            throw Exception(s"Stok barang dengan ID ${keranjangItemReq.barangId} tidak cukup")
//
//          println("sebelum ubah stop : " + barangData)
//          val barangUpdated = barangData.copy(stok = Some(currentStock - keranjangItemReq.jumlah))
//          println("setelah ubah stop : " + barangUpdated)
//        }
//        case None => throw Exception(s"Barang dengan ID ${keranjangItemReq.barangId} tidak ditemukan")
//      }
//
//      successMessage("hola")
//    } catch {
//      case e: Exception => BadRequest(Json.obj("status" -> "error", "message" -> e.getMessage))
//    }
  }

  def update: Action[AnyContent] = Action { implicit request =>
    Ok("ok")
//    val keranjang = request.body.asJson.get.validate[Keranjang].get
//    if (keranjangRepo.update(keranjang)) {
//      Ok(Json.obj("message" -> "Data diperbarui"))
//    } else {
//      BadRequest(Json.obj("message" -> "Gagal memperbarui"))
//    }
  }

  def delete(id: Int): Action[AnyContent] = Action {
    Ok("ok")
//    if (keranjangRepo.delete(id)) {
//      Ok(Json.obj("message" -> "Data dihapus"))
//    } else {
//      NotFound(Json.obj("message" -> "Data tidak ditemukan"))
//    }
  }
}
