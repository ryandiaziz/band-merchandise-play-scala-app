package models

import anorm.*
import anorm.SqlParser.*
import play.api.libs.json.{Json, OWrites, Reads}
import utils.JsonConfig
import java.time.LocalDateTime

case class Merchandise(
    id: Int = -1,
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
  implicit val merchandiseReads: Reads[Merchandise] = Json.reads[Merchandise]
  implicit val merchandiseWrites: OWrites[Merchandise] = OWrites[Merchandise] { merch =>
    Json.obj(
      "id"            -> merch.id,
      "title"         -> merch.title,
      "band_name"     -> merch.bandName,
      "merch_type_id" -> merch.merchTypeId,
      "price"         -> merch.price,
      "stock"         -> merch.stock,
      JsonConfig.optionalField("image_url", merch.imageUrl),
      JsonConfig.optionalField("description", merch.description),
      JsonConfig.optionalField("created_at", merch.createdAt.map(_.toString)),
      JsonConfig.optionalField("updated_at", merch.updatedAt.map(_.toString))
    )
  }
  
  val parser: RowParser[Merchandise] = (
    int("id") ~
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
  
  implicit val merchandiseRequestReads: Reads[MerchandiseRequest] = Json.reads[MerchandiseRequest]
}
