package models

import anorm.*
import anorm.SqlParser.*
import play.api.libs.json.*
import utils.JsonConfig._

case class MerchType(
    id: Option[Int] = None,
    name: String,
    description: Option[String]
)
object MerchType {
  implicit val reads: Reads[MerchType] = Json.reads[MerchType]

  implicit val writes: OWrites[MerchType] = OWrites[MerchType] { merch =>
    Json.obj(
      "id"   -> merch.id,
      "name" -> merch.name,
      optionalField("description", merch.description)
    )
  }

  val parser: RowParser[MerchType] = (
    int("id").? ~
      str("name") ~
      str("description").?
  ) map { case id ~ name ~ desc => MerchType(id, name, desc) }
}
