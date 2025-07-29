package repositories

import anorm.*
import models.{Cart, CartMerch}
import play.api.db.*
import repositories.base.BaseRepository
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartRepository @Inject() (db: Database)(implicit ec: ExecutionContext) extends BaseRepository(db) {
  override protected val tableName: String = "cart"
  private val cartMerchTableName           = "cart_merch"

  def create(cart: Cart): Future[Cart] = {
    executeInsert(
      s"INSERT INTO $tableName(user_id, price, status, created_at, updated_at) VALUES ({userId}, {price}, {status}, NOW(), NOW())",
      "userId" -> cart.userId,
      "price"  -> cart.price,
      "status" -> cart.status
    ).map { idOpt =>
      cart.copy(id = idOpt.get)
    }
  }

  def update(cart: Cart): Future[Option[Cart]] = {
    executeUpdate(
      s"UPDATE $tableName SET price = {price}, status = {status}, updated_at = NOW() WHERE id = {id}",
      "id"     -> cart.id,
      "price"  -> cart.price,
      "status" -> cart.status
    ).flatMap { affectedRows =>
      if (affectedRows > 0) findById(cart.id) else Future.successful(None)
    }
  }

  // --- Cart Merch Specific Operations ---
  def addMerchToCart(cartMerch: CartMerch): Future[CartMerch] = {
    executeInsert(
      s"""
        |INSERT INTO $cartMerchTableName(cart_id, merchandise_id, qty, unit_price, total_price, created_at, updated_at)
        |VALUES ({cartId}, {merchandiseId}, {qty}, {unitPrice}, {totalPrice}, NOW(), NOW())
        |""".stripMargin,
      "cartId"        -> cartMerch.cartId,
      "merchandiseId" -> cartMerch.merchandiseId,
      "qty"           -> cartMerch.qty,
      "unitPrice"     -> cartMerch.unitPrice,
      "totalPrice"    -> cartMerch.totalPrice
    ).map { idOpt =>
      cartMerch.copy(id = idOpt.get)
    }
  }

  def findCartMerchByCartId(cartId: Int): Future[Seq[CartMerch]] = {
    executeList[CartMerch](
      s"SELECT * FROM $cartMerchTableName WHERE cart_id = {cartId} AND is_delete = FALSE",
      "cartId" -> cartId
    )(CartMerch.parser)
  }

  def findCartMerchByCartIdAndMerchandiseId(cartId: Int, merchandiseId: Int): Future[Option[CartMerch]] = {
    executeSingle[CartMerch](
      s"SELECT * FROM $cartMerchTableName WHERE cart_id = {cartId} AND merchandise_id = {merchandiseId} AND is_delete = FALSE",
      "cartId"        -> cartId,
      "merchandiseId" -> merchandiseId
    )(CartMerch.parser)
  }

  def findUserActiveCart(userId: Int): Future[Option[Cart]] = {
    executeSingle[Cart](
      s"SELECT * FROM $tableName WHERE user_id = {userId} AND status = 'active' AND is_delete = FALSE",
      "userId" -> userId
    )(Cart.parser)
  }

  def updateCartMerchQty(cartMerchId: Int, newQty: Int, newTotalPrice: BigDecimal): Future[Int] = {
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

  def findById(id: Int): Future[Option[Cart]] = {
    super.findById[Cart](id)(Cart.parser)
  }

  def softDeleteCart(id: Int): Future[Boolean] = {
    super.softDelete(id)
  }
}
