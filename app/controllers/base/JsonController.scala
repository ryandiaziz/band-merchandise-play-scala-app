package controllers.base

import play.api.libs.json.*
import play.api.mvc.*

trait JsonController extends BaseControllerHelpers {

  // ======= SUCCESS =======

  def success(data: JsValue): Result = {
    Ok(
      Json.obj(
        "status" -> true,
        "data"   -> data
      )
    )
  }

  def successMessage(message: String): Result = {
    Ok(
      Json.obj(
        "status"  -> true,
        "message" -> message
      )
    )
  }

  def successWithMessage(data: JsValue, message: String = "Berhasil mendapatkan data"): Result = {
    Ok(
      Json.obj(
        "status"  -> true,
        "message" -> message,
        "data"    -> data
      )
    )
  }

  def created(data: JsValue, message: String = "Data berhasil dibuat"): Result = {
    Created(
      Json.obj(
        "status"  -> true,
        "message" -> message,
        "data"    -> data
      )
    )
  }

  // ======= ERROR / FAILURE =======

  def jsonMissingField(field: String): Result = {
    BadRequest(
      Json.obj(
        "status"  -> false,
        "message" -> s"Field '$field' harus ada dan tidak boleh kosong",
        "data"    -> JsNull
      )
    )
  }

  def jsonFormatError(detail: String): Result = {
    BadRequest(
      Json.obj(
        "status"  -> false,
        "message" -> "Format JSON tidak valid",
        "detail"  -> detail
      )
    )
  }

  def jsonExpected(): Result = {
    BadRequest(
      Json.obj(
        "status"  -> false,
        "message" -> "Body harus dalam format JSON",
        "data"    -> JsNull
      )
    )
  }

  def dbError(message: String = "Terjadi kesalahan saat menyimpan data"): Result = {
    InternalServerError(
      Json.obj(
        "status"  -> false,
        "message" -> message,
        "data"    -> JsNull
      )
    )
  }

  def notFoundError(entity: String): Result = {
    NotFound(
      Json.obj(
        "status"  -> false,
        "message" -> s"$entity tidak ditemukan",
        "data"    -> JsNull
      )
    )
  }

  def unauthorizedError(): Result = {
    Unauthorized(
      Json.obj(
        "status"  -> false,
        "message" -> "Tidak memiliki izin untuk melakukan aksi ini",
        "data"    -> JsNull
      )
    )
  }

  def serverError(detail: String): Result = {
    InternalServerError(
      Json.obj(
        "status"  -> false,
        "message" -> "Kesalahan server",
        "detail"  -> detail
      )
    )
  }

  def customResponse(
      statusCode: Int,
      status: Boolean,
      message: Option[String] = None,
      data: Option[JsValue] = None
  ): Result = {
    val base        = Json.obj("status" -> status)
    val withMessage = message.map(m => base + ("message" -> JsString(m))).getOrElse(base)
    val withData    = data.map(d => withMessage + ("data" -> d)).getOrElse(withMessage)
    Results.Status(statusCode)(withData)
  }
}
