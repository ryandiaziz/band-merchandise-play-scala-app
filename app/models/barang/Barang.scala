package models.barang

import anorm.*
import anorm.SqlParser.*
import play.api.libs.json.{Json, OFormat}

case class Barang(
    id: Int = -1,
    nama: String,
    kategoriId: Int,
    harga: Option[Double],
    stok: Option[Long],
    isDelete: Boolean = false
)

object Barang {
  implicit val barangFormat: OFormat[Barang] = Json.format[Barang]

  val parser: RowParser[Barang] = (
    int("barang.barang_id") ~ // Kolom 'barang_id' diubah menjadi Int untuk field 'id'
      str("barang.nama") ~ // Kolom 'nama' diubah menjadi String
      int("barang.kategori_id") ~ // Kolom 'kategori_id' diubah menjadi Int
      double("barang.harga") ~ // Kolom 'harga' diubah menjadi BigDecimal
      int("barang.stok") ~ // Kolom 'stok' diubah menjadi Int
      bool("barang.is_delete") // Kolom 'is_delete' diubah menjadi Boolean
    ) map {
    case id ~ nama ~ kategoriId ~ harga ~ stok ~ isDelete =>
      Barang(id, nama, kategoriId, Some(harga), Some(stok), isDelete)
  }
}
