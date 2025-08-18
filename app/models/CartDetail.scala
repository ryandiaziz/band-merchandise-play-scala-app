package models

import anorm.*
import anorm.SqlParser.*
import play.api.libs.json.{Json, OWrites}
import utils.JsonConfig

import java.time.LocalDateTime

case class CartDetail(
    cart_id: Long,
    cart_total_price: BigDecimal,
    status: String,
    created_at: Option[LocalDateTime] = None,
    user: User,
    items: Seq[CartItem]
)

object CartDetail {
  case class ParseTemp(
      cartId: Long,
      cartTotalPrice: BigDecimal,
      status: String,
      cartCreatedAt: Option[LocalDateTime] = None,
      user: User,
      cartItem: CartItem
  )

  val parser: RowParser[ParseTemp] = (
    Cart.parser ~
      User.parser ~
      CartMerch.parser ~
      Merchandise.parser
  ) map { case cart ~ user ~ cartMerch ~ merchandise =>
    ParseTemp(
      cartId = cart.id,
      cartTotalPrice = cart.price,
      status = cart.status,
      cartCreatedAt = cart.createdAt,
      user = user,
      cartItem = CartItem(
        merchandise_id = merchandise.id,
        title = merchandise.title,
        band_name = merchandise.bandName,
        description = merchandise.description,
        image_url = merchandise.imageUrl,
        qty = cartMerch.qty,
        unit_price = cartMerch.unitPrice
      )
    )
  }

  implicit val cartDetailWrites: OWrites[CartDetail] = OWrites[CartDetail] { cartDetail =>
    Json.obj(
      "cart_id"          -> cartDetail.cart_id,
      "cart_total_price" -> cartDetail.cart_total_price,
      "status"           -> cartDetail.status,
      "user"             -> Json.toJson(cartDetail.user),
      "items"            -> Json.toJson(cartDetail.items),
      JsonConfig.optionalField("created_at", cartDetail.created_at.map(_.toString))
    )
  }
}
