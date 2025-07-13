package models.kategori

import anorm.*
import anorm.SqlParser.*
import play.api.libs.json.{Json, OFormat}

case class Kategori(
    id: Int,
    nama: String,
    isDelete: Boolean = false
)

object Kategori {
  implicit val kategoriFormat: OFormat[Kategori] = Json.format[Kategori]
  
  val parser: RowParser[Kategori] = {
    get[Int]("kategori.kategori_id") ~
      get[String]("kategori.nama") ~
      get[Boolean]("kategori.is_delete") map { case id ~ nama ~ isDelete =>
        Kategori(id, nama, isDelete)
      }
  }
}
