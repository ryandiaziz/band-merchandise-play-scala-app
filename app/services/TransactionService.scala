package services

import models.Transaction
import play.api.db.Database
import repositories.{CartRepository, TransactionRepository}

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransactionService @Inject() (
    db: Database,
    transactionRepo: TransactionRepository,
    cartRepo: CartRepository
)(implicit ec: ExecutionContext) {

  def createTransaction(request: Transaction.CreateTransactionRequest): Future[Option[Transaction]] = Future {
    db.withTransaction { implicit connection =>
      val cart =
        cartRepo.findById(request.cartId).getOrElse(throw new Exception(s"Cart with ID ${request.cartId} not found."))

      if (cart.status != "active") {
        throw new Exception(s"Cart with ID ${request.cartId} is not active and cannot be transacted.")
      } else if (cart.price <= BigDecimal(0)) {
        throw new Exception(s"Cart with ID ${request.cartId} has no items or total price is zero.")
      }

      val existingTransaction = transactionRepo.findByCartId(request.cartId)
      if (existingTransaction.isDefined) {
        throw new Exception(s"A transaction for cart ID ${request.cartId} already exists.")
      }

      val totalPrice = cart.price + request.deliveryServicePrice
      val newTransaction = Transaction(
        cartId = request.cartId,
        cartPrice = cart.price,
        deliveryServicePrice = request.deliveryServicePrice,
        totalPrice = totalPrice
      )
      val createdTxn = transactionRepo.create(newTransaction)

      cartRepo.update(cart.copy(status = "ordered")).get

      Some(createdTxn)
    }
  }

  def getTransaction(id: Int): Future[Option[Transaction]] = Future {
    db.withConnection { implicit connection =>
      transactionRepo.findById(id)
    }
  }

  def getAllTransactions: Future[Seq[Transaction]] = Future {
    db.withConnection { implicit connection =>
      transactionRepo.findAll()
    }
  }

  def updateTransaction(id: Int, transaction: Transaction): Future[Option[Transaction]] = Future {
    db.withTransaction { implicit connection =>
      val existingTxn = transactionRepo.findById(id)
      existingTxn.flatMap { txn =>
        val updatedTxn = txn.copy(
          cartPrice = transaction.cartPrice,
          deliveryServicePrice = transaction.deliveryServicePrice,
          totalPrice = transaction.totalPrice
        )
        transactionRepo.update(updatedTxn)
      }
    }
  }

  def softDeleteTransaction(id: Int): Future[Boolean] = Future {
    db.withTransaction { implicit connection =>
      transactionRepo.softDelete(id)
    }
  }
}
