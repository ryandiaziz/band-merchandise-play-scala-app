package repositories

import anorm.*
import models.Transaction
import repositories.base.BaseRepositoryNew

import java.sql.Connection
import javax.inject.*
import scala.concurrent.ExecutionContext

@Singleton
class TransactionRepository @Inject() ()(implicit ec: ExecutionContext) extends BaseRepositoryNew() {
  override protected val tableName: String = "transactions"

  def create(transaction: Transaction)(implicit connection: Connection): Transaction = {
    val resultId = executeInsert(
      s"""
        |INSERT INTO $tableName(cart_id, cart_price, delivery_service_price, total_price, created_at, updated_at)
        |VALUES ({cart_id}, {cartPrice}, {deliveryServicePrice}, {totalPrice}, NOW(), NOW())
        |""".stripMargin,
      "cart_id"               -> transaction.cartId,
      "cartPrice"            -> transaction.cartPrice,
      "deliveryServicePrice" -> transaction.deliveryServicePrice,
      "totalPrice"           -> transaction.totalPrice
    )

    transaction.copy(id = resultId.get)
  }

  def findByCartId(cartId: Int)(implicit connection: Connection): Option[Transaction] = {
    executeSingle[Transaction](
      s"SELECT * FROM $tableName WHERE cart_id = {cart_id} AND is_delete = FALSE",
      "cart_id" -> cartId
    )(Transaction.parser)
  }

  def update(transaction: Transaction)(implicit connection: Connection): Option[Transaction] = {
    val affectedRows = executeUpdate(
      s"""
      |UPDATE $tableName
      |SET cart_price = {cartPrice}, delivery_service_price = {deliveryServicePrice},
      |total_price = {totalPrice}, updated_at = NOW()
      |WHERE id = {id}
      |""".stripMargin,
      "id"                   -> transaction.id,
      "cartPrice"            -> transaction.cartPrice,
      "deliveryServicePrice" -> transaction.deliveryServicePrice,
      "totalPrice"           -> transaction.totalPrice
    )

    if (affectedRows > 0) findById(transaction.id) else None
  }

  def findById(id: Int)(implicit connection: Connection): Option[Transaction] = {
    super.findById[Transaction](id)(Transaction.parser)
  }

  def findAll()(implicit connection: Connection): Seq[Transaction] = {
    super.findAll[Transaction](Transaction.parser)
  }

  def deleteTransaction(id: Int)(implicit connection: Connection): Int = {
    super.delete(id)
  }

  def softDeleteTransaction(id: Int)(implicit connection: Connection): Boolean = {
    super.softDelete(id)
  }
}
