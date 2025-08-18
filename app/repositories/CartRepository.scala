package repositories

import anorm.*
import anorm.SqlParser.long
import models.{Cart, CartDetail, CartItem, CartMerch}
import repositories.base.BaseRepositoryNew

import java.sql.Connection
import javax.inject.*
import scala.concurrent.ExecutionContext

@Singleton
class CartRepository @Inject() ()(implicit ec: ExecutionContext) extends BaseRepositoryNew() {
  override protected val tableName: String = "cart"
  private val cartMerchTableName           = "cart_merch"

  def create(cart: Cart)(implicit connection: Connection): Cart = {
    val resultId = executeInsert(
      s"INSERT INTO $tableName(user_id, price, status, created_at, updated_at) VALUES ({userId}, {price}, {status}, NOW(), NOW())",
      "userId" -> cart.userId,
      "price"  -> cart.price,
      "status" -> cart.status
    )

    cart.copy(id = resultId.get)
  }

  def update(cart: Cart)(implicit connection: Connection): Option[Cart] = {
    val affectedRows = executeUpdate(
      s"UPDATE $tableName SET price = {price}, status = {status}, updated_at = NOW() WHERE id = {id}",
      "id"     -> cart.id,
      "price"  -> cart.price,
      "status" -> cart.status
    )

    if (affectedRows > 0) findById(cart.id) else None
  }

  def addMerchToCart(cartMerch: CartMerch)(implicit connection: Connection): CartMerch = {
    val resultId = executeInsert(
      s"""
        |INSERT INTO $cartMerchTableName(cart_id, merchandise_id, qty, unit_price, total_price, created_at, updated_at)
        |VALUES ({cart_id}, {merchandiseId}, {qty}, {unitPrice}, {totalPrice}, NOW(), NOW())
        |""".stripMargin,
      "cart_id"        -> cartMerch.cartId,
      "merchandiseId" -> cartMerch.merchandiseId,
      "qty"           -> cartMerch.qty,
      "unitPrice"     -> cartMerch.unitPrice,
      "totalPrice"    -> cartMerch.totalPrice
    )

    cartMerch.copy(id = resultId.get)
  }

  def findCartMerchByCartId(cartId: Int)(implicit connection: Connection): Seq[CartMerch] = {
    executeList[CartMerch](
      s"SELECT * FROM $cartMerchTableName WHERE cart_id = {cart_id} AND is_delete = FALSE",
      "cart_id" -> cartId
    )(CartMerch.parser)
  }

  def findCartMerchByCartIdAndMerchandiseId(cartId: Int, merchandiseId: Int)(implicit
      connection: Connection
  ): Option[CartMerch] = {
    executeSingle[CartMerch](
      s"SELECT * FROM $cartMerchTableName WHERE cart_id = {cart_id} AND merchandise_id = {merchandiseId} AND is_delete = FALSE",
      "cart_id"        -> cartId,
      "merchandiseId" -> merchandiseId
    )(CartMerch.parser)
  }

  def findUserActiveCart(userId: Int)(implicit connection: Connection): Option[Cart] = {
    executeSingle[Cart](
      s"SELECT * FROM $tableName WHERE user_id = {userId} AND status = 'active' AND is_delete = FALSE",
      "userId" -> userId
    )(Cart.parser)
  }

  def findUserActiveCartDetail(userId: Int)(implicit connection: Connection): Option[Cart] = {
    executeSingle[Cart](
      s"SELECT * FROM $tableName WHERE user_id = {userId} AND status = 'active' AND is_delete = FALSE",
      "userId" -> userId
    )(Cart.parser)
  }

  def updateCartMerchQty(cartMerchId: Int, newQty: Int, newTotalPrice: BigDecimal)(implicit
      connection: Connection
  ): Int = {
    executeUpdate(
      s"""
        |UPDATE $cartMerchTableName
        |SET qty = {qty}, total_price = {totalPrice}, updated_at = NOW()
        |WHERE id = {id}
        |""".stripMargin,
      "id"         -> cartMerchId,
      "qty"        -> newQty,
      "totalPrice" -> newTotalPrice
    )
  }

  def findByIdDetail(id: Int)(implicit connection: Connection): Seq[CartDetail.ParseTemp] = {
    val query = """
        SELECT c.*, u.*, cm.*, m.*
        FROM
            cart AS c
        JOIN
            users AS u ON c.user_id = u.id
        JOIN
            cart_merch AS cm ON c.id = cm.cart_id
        JOIN
            merchandise AS m ON cm.merchandise_id = m.id
        WHERE
            c.id = {cart_id}
      """

    executeList[CartDetail.ParseTemp](
      query,
      "cart_id" -> id
    )(CartDetail.parser)
  }

  def findById(id: Int)(implicit connection: Connection): Option[Cart] = {
    super.findById[Cart](id)(Cart.parser)
  }

  def softDeleteCart(id: Int)(implicit connection: Connection): Boolean = {
    super.softDelete(id)
  }
}
