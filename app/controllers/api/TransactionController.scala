package controllers.api

import controllers.base.JsonController
import models.Transaction
import play.api.libs.json.*
import play.api.mvc.*
import services.TransactionService

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransactionController @Inject() (
    val controllerComponents: ControllerComponents,
    transactionService: TransactionService
)(implicit ec: ExecutionContext)
    extends BaseController
    with JsonController {

  def createTransaction() = Action(parse.json).async { implicit request =>
    request.body
      .validate[Transaction.CreateTransactionRequest]
      .fold(
        errors =>
          Future.successful(
            jsonFormatError(JsError.toJson(errors).toString)
          ),
        transactionReq =>
          transactionService.createTransaction(transactionReq).map {
            case Right(transaction) => created(Json.toJson(transaction))
            case Left(errorMessage) => notFoundError(errorMessage)
          }
      )
  }

  def getTransaction(id: Int) = Action.async {
    transactionService.getTransaction(id).map {
      case Some(transaction) => successWithMessage(Json.toJson(transaction))
      case None              => notFoundError(s"Transaction ID $id")
    }
  }

  def getAllTransactions() = Action.async {
    transactionService.getAllTransactions().map { transactions =>
      successWithMessage(Json.toJson(transactions))
    }
  }

  def updateTransaction(id: Int) = Action(parse.json).async { implicit request =>
    request.body
      .validate[Transaction]
      .fold(
        errors =>
          Future.successful(
            jsonFormatError(JsError.toJson(errors).toString)
          ),
        transactionUpdate =>
          transactionService.updateTransaction(id, transactionUpdate).map {
            case Some(updatedTransaction) => created(Json.toJson(updatedTransaction))
            case None                     => notFoundError(s"Transaction $id tidak ada perubahan atau")
          }
      )
  }

  def deleteTransaction(id: Int) = Action.async {
    transactionService.softDeleteTransaction(id).map { success =>
      if (success) successMessage(s"Transaction ID $id deleted successfully")
      else notFoundError(s"Transaction ID $id")
    }
  }
}
