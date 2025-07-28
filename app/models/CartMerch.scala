package models

import anorm.*
import anorm.SqlParser.*
import play.api.libs.json.{Json, OWrites, Reads}
import utils.JsonConfig

import java.time.LocalDateTime

case class CartMerch(
    id: Int = -1,
    cartId: Int,
    merchandiseId: Int,
    qty: Int,
    unitPrice: BigDecimal,
    totalPrice: BigDecimal,
    createdAt: Option[LocalDateTime] = None,
    updatedAt: Option[LocalDateTime] = None
)

object CartMerch {
  implicit val cartMerchReads: Reads[CartMerch] = Json.reads[CartMerch]
  implicit val cartMerchWrites: OWrites[CartMerch] = OWrites[CartMerch] { cartMerch =>
    Json.obj(
      "id"             -> cartMerch.id,
      "cart_id"        -> cartMerch.cartId,
      "merchandise_id" -> cartMerch.merchandiseId,
      "qty"            -> cartMerch.qty,
      "unit_price"     -> cartMerch.unitPrice,
      "total_price"    -> cartMerch.totalPrice,
      JsonConfig.optionalField("created_at", cartMerch.createdAt.map(_.toString)),
      JsonConfig.optionalField("updated_at", cartMerch.updatedAt.map(_.toString))
    )
  }
  val parser: RowParser[CartMerch] = (
    int("id") ~
      int("cart_id") ~
      int("merchandise_id") ~
      int("qty") ~
      get[BigDecimal]("unit_price") ~
      get[BigDecimal]("total_price") ~
      get[LocalDateTime]("created_at").? ~
      get[LocalDateTime]("updated_at").?
  ) map { case id ~ cartId ~ merchandiseId ~ qty ~ unitPrice ~ totalPrice ~ createdAt ~ updatedAt =>
    CartMerch(id, cartId, merchandiseId, qty, unitPrice, totalPrice, createdAt, updatedAt)
  }

  case class AddItemToCartRequest(
      userId: Int,
      merchandiseId: Int,
      qty: Int,
      cartId: Option[Int]
  )

  implicit val addItemToCartRequestReads: Reads[AddItemToCartRequest] = Json.reads[AddItemToCartRequest]
}
