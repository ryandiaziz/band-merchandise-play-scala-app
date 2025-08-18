package models

import play.api.libs.json.{Json, OWrites}
import utils.JsonConfig

case class CartItem(
    merchandiseId: Long,
    title: String,
    bandName: String,
    description: Option[String],
    imageUrl: Option[String],
    qty: Int,
    unitPrice: BigDecimal
)

object CartItem {
  implicit val cartItemWrites: OWrites[CartItem] = OWrites[CartItem] { item =>
    Json.obj(
      "merchandiseId" -> item.merchandiseId,
      "title"         -> item.title,
      "bandName"      -> item.bandName,
      "qty"           -> item.qty,
      "unitPrice"     -> item.unitPrice,
      JsonConfig.optionalField("description", item.description),
      JsonConfig.optionalField("imageUrl", item.imageUrl)
    )
  }
}
