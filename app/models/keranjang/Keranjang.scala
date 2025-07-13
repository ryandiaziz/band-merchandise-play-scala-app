package models.keranjang

import anorm.SqlParser.*
import anorm.*
import play.api.libs.json.{Json, OFormat}

case class Keranjang(
    keranjangId: Int = -1,
    userId: Int,
    totalHarga: Double,
    isDelete: Boolean = false
)

object Keranjang {
  implicit val keranjangFormat: OFormat[Keranjang] = Json.format[Keranjang]

  val parser: RowParser[Keranjang] = (
    int("keranjang.keranjang_id") ~
      int("keranjang.user_id") ~
      double("keranjang.total_harga") ~
      bool("barang.is_delete")
  ) map { case id ~ userId ~ totalHarga ~ isDelete =>
    Keranjang(id, userId, totalHarga, isDelete)
  }
}
