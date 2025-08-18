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
    int("merchandise.id") ~
      str("merchandise.title") ~
      str("merchandise.band_name") ~
      int("merchandise.merch_type_id") ~
      str("merchandise.description").? ~
      get[BigDecimal]("merchandise.price") ~
      str("merchandise.image_url").? ~
      int("merchandise.stock") ~
      get[LocalDateTime]("merchandise.created_at").? ~
      get[LocalDateTime]("merchandise.updated_at").?
  ) map {
    case merchandiseId ~ title ~ bandName ~ merchTypeId ~ desc ~ price ~ imageUrl ~ stock ~ createdAt ~ updatedAt =>
      Merchandise(merchandiseId, title, bandName, merchTypeId, desc, price, imageUrl, stock, createdAt, updatedAt)
  }

  case class MerchandiseWithMerchType(
      id: Int,
      title: String,
      bandName: String,
      merchType: MerchType,
      description: Option[String],
      price: BigDecimal,
      imageUrl: Option[String],
      stock: Int,
      createdAt: Option[LocalDateTime],
      updatedAt: Option[LocalDateTime]
  )

  implicit val merchWithMerchTypeWrites: OWrites[MerchandiseWithMerchType] = OWrites[MerchandiseWithMerchType] {
    merch =>
      Json.obj(
        "id"         -> merch.id,
        "title"      -> merch.title,
        "band_name"  -> merch.bandName,
        "merch_type" -> merch.merchType,
        "price"      -> merch.price,
        "stock"      -> merch.stock,
        JsonConfig.optionalField("image_url", merch.imageUrl),
        JsonConfig.optionalField("description", merch.description),
        JsonConfig.optionalField("created_at", merch.createdAt.map(_.toString)),
        JsonConfig.optionalField("updated_at", merch.updatedAt.map(_.toString))
      )
  }

  val merchWithMerchTypeParser: RowParser[MerchandiseWithMerchType] = (
    Merchandise.parser ~
      MerchType.parser
  ) map { case merch ~ merchType =>
    MerchandiseWithMerchType(
      merch.id,
      merch.title,
      merch.bandName,
      merchType,
      merch.description,
      merch.price,
      merch.imageUrl,
      merch.stock,
      merch.createdAt,
      merch.updatedAt
    )
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
