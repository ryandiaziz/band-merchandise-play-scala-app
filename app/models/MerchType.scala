package models

import anorm.*
import anorm.SqlParser.*
import play.api.libs.json.*
import utils.JsonConfig

import java.time.LocalDateTime

case class MerchType(
    id: Int = -1,
    name: String,
    description: Option[String],
    createdAt: Option[LocalDateTime] = None,
    updatedAt: Option[LocalDateTime] = None
)
object MerchType {
  implicit val reads: Reads[MerchType] = Json.reads[MerchType]

  implicit val writes: OWrites[MerchType] = OWrites[MerchType] { merch =>
    Json.obj(
      "id"   -> merch.id,
      "name" -> merch.name,
      JsonConfig.optionalField("description", merch.description),
      JsonConfig.optionalField("created_at", merch.createdAt),
      JsonConfig.optionalField("updated_at", merch.updatedAt)
    )
  }

  val parser: RowParser[MerchType] = (
    int("id") ~
      str("name") ~
      str("description").? ~
      get[LocalDateTime]("created_at").? ~
      get[LocalDateTime]("updated_at").?
  ) map { case id ~ name ~ desc ~ createdAt ~ updatedAt =>
    MerchType(id, name, desc, createdAt, updatedAt)
  }
}
