package models

import anorm.*
import anorm.SqlParser.*
import play.api.libs.json.{Json, OFormat, Reads}

import java.time.LocalDateTime

case class Merchandise(
    id: Option[Int] = None,
    title: String,
    bandName: String,
    merchTypeId: Int,
    description: Option[String],
    price: BigDecimal,
    imageUrl: Option[String],
    stock: Int,
    createdAt: Option[LocalDateTime] = None,
    updatedAt: Option[LocalDateTime] = None
)

object Merchandise {
  implicit val format: OFormat[Merchandise]            = Json.format[Merchandise]
  implicit val requestReads: Reads[MerchandiseRequest] = Json.reads[MerchandiseRequest]

  val parser: RowParser[Merchandise] = (
    int("id").? ~
      str("title") ~
      str("band_name") ~
      int("merch_type_id") ~
      str("description").? ~
      get[BigDecimal]("price") ~
      str("image_url").? ~
      int("stock") ~
      get[LocalDateTime]("created_at").? ~
      get[LocalDateTime]("updated_at").?
  ) map { case id ~ title ~ bandName ~ merchTypeId ~ desc ~ price ~ imageUrl ~ stock ~ createdAt ~ updatedAt =>
    Merchandise(id, title, bandName, merchTypeId, desc, price, imageUrl, stock, createdAt, updatedAt)
  }

  case class MerchandiseRequest(
      title: String,
      bandName: String,
      merchTypeId: Int,
      description: Option[String],
      price: BigDecimal,
      imageUrl: Option[String],
      stock: Int
  )
}
