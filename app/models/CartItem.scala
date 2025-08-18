package models

import play.api.libs.json.{Json, OWrites}
import utils.JsonConfig

case class CartItem(
    merchandise_id: Long,
    title: String,
    band_name: String,
    description: Option[String],
    image_url: Option[String],
    qty: Int,
    unit_price: BigDecimal
)

object CartItem {
  implicit val cartItemWrites: OWrites[CartItem] = OWrites[CartItem] { item =>
    Json.obj(
      "merchandise_id" -> item.merchandise_id,
      "title"          -> item.title,
      "band_name"      -> item.band_name,
      "qty"            -> item.qty,
      "unit_price"     -> item.unit_price,
      JsonConfig.optionalField("description", item.description),
      JsonConfig.optionalField("image_url", item.image_url)
    )
  }
}
