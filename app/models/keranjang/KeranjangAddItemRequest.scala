package models.keranjang

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}

case class KeranjangAddItemRequest(
    userId: Int,
    barangId: Int,
    jumlah: Int,
    keranjangId: Option[Long]
)

// Definisikan implicit Reads untuk parsing JSON ke KeranjangAddItemRequest
object KeranjangAddItemRequest {
  implicit val reads: Reads[KeranjangAddItemRequest] = (
    (JsPath \ "user_id").read[Int] and
    (JsPath \ "barang_id").read[Int] and
    (JsPath \ "jumlah").read[Int] and
    (JsPath \ "keranjang_id").readNullable[Long]
  )(KeranjangAddItemRequest.apply _)
}