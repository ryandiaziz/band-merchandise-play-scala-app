package models.keranjang

import anorm.SqlParser.*
import anorm.*
import play.api.libs.json.{Json, OFormat}

case class KeranjangItem(
    keranjangItemId: Int = -1,
    keranjangId: Int,
    barangId: Int,
    jumlah: Double,
    unitHarga: Double,
    totalHargaItem: Double
)

object KeranjangItem {
  implicit val itemKeranjangFormat: OFormat[KeranjangItem] = Json.format[KeranjangItem]

  val parser: RowParser[KeranjangItem] = (
    int("item_keranjang.item_keranjang_id") ~
      int("item_keranjang.keranjang_id") ~
      int("item_keranjang.barang_id") ~
      double("item_keranjang.jumlah") ~
      double("item_keranjang.unit_harga") ~
      double("item_keranjang.total_harga_item")
  ) map { case itemKeranjangId ~ keranjangId ~ barangId ~ jumlah ~ unitHarga ~ totalHargaItem =>
    KeranjangItem(itemKeranjangId, keranjangId, barangId, jumlah, unitHarga, totalHargaItem)
  }
}
