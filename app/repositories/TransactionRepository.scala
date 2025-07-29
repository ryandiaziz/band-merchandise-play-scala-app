package repositories

import anorm.*
import models.Transaction
import play.api.db.*
import repositories.base.BaseRepository
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransactionRepository @Inject() (db: Database)(implicit ec: ExecutionContext) extends BaseRepository(db) {
  override protected val tableName: String = "transactions"

  def create(transaction: Transaction): Future[Transaction] = {
    executeInsert(
      s"""
        |INSERT INTO $tableName(cart_id, cart_price, delivery_service_price, total_price, created_at, updated_at)
        |VALUES ({cartId}, {cartPrice}, {deliveryServicePrice}, {totalPrice}, NOW(), NOW())
        |""".stripMargin,
      "cartId"               -> transaction.cartId,
      "cartPrice"            -> transaction.cartPrice,
      "deliveryServicePrice" -> transaction.deliveryServicePrice,
      "totalPrice"           -> transaction.totalPrice
    ).map { idOpt =>
      transaction.copy(id = idOpt.get)
    }
  }

  def findByCartId(cartId: Int): Future[Option[Transaction]] = {
    executeSingle[Transaction](
      s"SELECT * FROM $tableName WHERE cart_id = {cartId} AND is_delete = FALSE",
      "cartId" -> cartId
    )(Transaction.parser)
  }

  def update(transaction: Transaction): Future[Option[Transaction]] = {
    executeUpdate(
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
    ).flatMap { affectedRows =>
      if (affectedRows > 0) findById(transaction.id) else Future.successful(None)
    }
  }

  def findById(id: Int): Future[Option[Transaction]] = {
    super.findById[Transaction](id)(Transaction.parser)
  }
  
  def findAll(): Future[Seq[Transaction]] = {
    super.findAll[Transaction](Transaction.parser)
  }

  def deleteTransaction(id: Int): Future[Int] = {
    super.delete(id)
  }

  def softDeleteTransaction(id: Int): Future[Boolean] = {
    super.softDelete(id)
  }
}
