package models

import anorm.*
import anorm.SqlParser.*
import play.api.libs.json.{Json, OWrites, Reads}
import utils.JsonConfig

import java.time.LocalDateTime

case class Cart(
    id: Int = -1,
    userId: Int,
    price: BigDecimal,
    status: String, // active, ordered, cancelled
    createdAt: Option[LocalDateTime] = None,
    updatedAt: Option[LocalDateTime] = None
)

object Cart {
  implicit val cartReads: Reads[Cart] = Json.reads[Cart]
  implicit val cartWrites: OWrites[Cart] = OWrites[Cart] { cart =>
    Json.obj(
      "id"      -> cart.id,
      "user_id" -> cart.userId,
      "price"   -> cart.price,
      "status"  -> cart.status,
      JsonConfig.optionalField("created_at", cart.createdAt.map(_.toString)),
      JsonConfig.optionalField("updated_at", cart.updatedAt.map(_.toString))
    )
  }
  val parser: RowParser[Cart] = (
    int("cart.id") ~
      int("cart.user_id") ~
      get[BigDecimal]("cart.price") ~
      str("cart.status") ~
      get[LocalDateTime]("cart.created_at").? ~
      get[LocalDateTime]("cart.updated_at").?
  ) map { case id ~ userId ~ price ~ status ~ createdAt ~ updatedAt =>
    Cart(id, userId, price, status, createdAt, updatedAt)
  }
}
