package models.barang

import models.kategori.Kategori
import anorm.*
import anorm.SqlParser.*
import play.api.libs.json.{Json, OFormat}

case class BarangWithKategori(
    id: Int,
    nama: String,
    harga: Option[Double],
    stok: Option[Long],
    isDelete: Boolean,
    kategori: Kategori
)

object BarangWithKategori {
  implicit val barangWithKategoriFormat: OFormat[BarangWithKategori] = Json.format[BarangWithKategori]

  val parser: RowParser[BarangWithKategori] = {
    Barang.parser ~
      Kategori.parser map {
        case barang ~
            kategori =>
          BarangWithKategori(
            id = barang.id,
            nama = barang.nama,
            harga = barang.harga,
            stok = barang.stok,
            isDelete = barang.isDelete,
            kategori = Kategori(
              id = kategori.id,
              nama = kategori.nama,
              isDelete = kategori.isDelete
            )
          )
      }
  }
}
