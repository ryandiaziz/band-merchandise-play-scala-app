package services

import models.{Cart, Transaction}
import repositories.{CartRepository, TransactionRepository}

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransactionService @Inject() (
    transactionRepo: TransactionRepository,
    cartRepo: CartRepository
)(implicit ec: ExecutionContext) {

  def createTransaction(request: Transaction.CreateTransactionRequest): Future[Either[String, Transaction]] = {
    cartRepo.findById(request.cartId).flatMap {
      case Some(cart) =>
        if (cart.status != "active") {
          Future.successful(Left(s"Cart with ID ${request.cartId} is not active and cannot be transacted."))
        } else if (cart.price <= BigDecimal(0)) {
          Future.successful(Left(s"Cart with ID ${request.cartId} has no items or total price is zero."))
        } else {
          transactionRepo.findByCartId(request.cartId).flatMap {
            case Some(_) => Future.successful(Left(s"A transaction for cart ID ${request.cartId} already exists."))
            case None =>
              val totalPrice = cart.price + request.deliveryServicePrice
              val newTransaction = Transaction(
                cartId = request.cartId,
                cartPrice = cart.price,
                deliveryServicePrice = request.deliveryServicePrice,
                totalPrice = totalPrice
              )
              transactionRepo.create(newTransaction).flatMap { createdTxn =>
                cartRepo
                  .update(
                    cart.copy(status = "ordered")
                  )
                  .map { _ =>
                    Right(createdTxn)
                  }
              }
          }
        }
      case None =>
        Future.successful(Left(s"Cart with ID ${request.cartId} not found."))
    }
  }

  def getTransaction(id: Int): Future[Option[Transaction]] = transactionRepo.findById(id)
  def getAllTransactions(): Future[Seq[Transaction]]       = transactionRepo.findAll()

  def updateTransaction(id: Int, transaction: Transaction): Future[Option[Transaction]] = {
    transactionRepo.findById(id).flatMap {
      case Some(existingTxn) =>
        val updatedTxn = existingTxn.copy(
          cartPrice = transaction.cartPrice,
          deliveryServicePrice = transaction.deliveryServicePrice,
          totalPrice = transaction.totalPrice
        )
        transactionRepo.update(updatedTxn)
      case None => Future.successful(None)
    }
  }

  def softDeleteTransaction(id: Int): Future[Boolean] = transactionRepo.softDelete(id)
}
