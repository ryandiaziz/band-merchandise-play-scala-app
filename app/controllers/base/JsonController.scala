package controllers.base

import play.api.libs.json.*
import play.api.mvc.*

trait JsonController extends BaseControllerHelpers {

  def success(data: JsValue): Result = {
    Ok(
      Json.obj(
        "status" -> "success",
        "data"   -> data
      )
    )
  }

  def successMessage(message: String): Result = {
    Ok(
      Json.obj(
        "status"  -> "success",
        "message" -> message
      )
    )
  }

  def successWithMessage(message: String, data: JsValue): Result = {
    Ok(
      Json.obj(
        "status"  -> "success",
        "message" -> message,
        "data"    -> data
      )
    )
  }

  def jsonMissingField(field: String): Result = {
    BadRequest(
      Json.obj(
        "status"  -> "error",
        "message" -> s"Field '$field' harus ada dan tidak boleh kosong"
      )
    )
  }

  def jsonFormatError(detail: String): Result = {
    BadRequest(
      Json.obj(
        "status"  -> "error",
        "message" -> "Format JSON tidak valid",
        "detail"  -> detail
      )
    )
  }

  def jsonExpected(): Result = {
    BadRequest(
      Json.obj(
        "status"  -> "error",
        "message" -> "Body harus dalam format JSON"
      )
    )
  }

  def dbError(): Result = {
    InternalServerError(
      Json.obj(
        "status"  -> "error",
        "message" -> "Terjadi kesalahan saat menyimpan data"
      )
    )
  }

  def notFoundError(entity: String): Result = {
    NotFound(
      Json.obj(
        "status"  -> "error",
        "message" -> s"$entity tidak ditemukan"
      )
    )
  }

  def unauthorizedError(): Result = {
    Unauthorized(
      Json.obj(
        "status"  -> "error",
        "message" -> "Tidak memiliki izin untuk melakukan aksi ini"
      )
    )
  }

  def serverError(detail: String): Result = {
    InternalServerError(
      Json.obj(
        "status"  -> "error",
        "message" -> "Kesalahan server",
        "detail"  -> detail
      )
    )
  }
}
