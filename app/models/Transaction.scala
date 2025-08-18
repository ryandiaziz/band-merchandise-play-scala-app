package models

import anorm.*
import anorm.SqlParser.*
import play.api.libs.json.{Json, OWrites, Reads}
import utils.JsonConfig

import java.time.LocalDateTime

case class Transaction(
    id: Int = -1,
    cartId: Int,
    cartPrice: BigDecimal,
    deliveryServicePrice: BigDecimal,
    totalPrice: BigDecimal,
    createdAt: Option[LocalDateTime] = None,
    updatedAt: Option[LocalDateTime] = None
)

object Transaction {
  implicit val transactionReads: Reads[Transaction] = Json.reads[Transaction]
  implicit val transactionWrites: OWrites[Transaction] = OWrites[Transaction] { transaction =>
    Json.obj(
      "id"                     -> transaction.id,
      "cart_id"                -> transaction.cartId,
      "cart_price"             -> transaction.cartPrice,
      "delivery_service_price" -> transaction.deliveryServicePrice,
      "total_price"            -> transaction.totalPrice,
      JsonConfig.optionalField("created_at", transaction.createdAt.map(_.toString)),
      JsonConfig.optionalField("updated_at", transaction.updatedAt.map(_.toString))
    )
  }

  val parser: RowParser[Transaction] = (
    int("transactions.id") ~
      int("transactions.cart_id") ~
      get[BigDecimal]("transactions.cart_price") ~
      get[BigDecimal]("transactions.delivery_service_price") ~
      get[BigDecimal]("transactions.total_price") ~
      get[LocalDateTime]("transactions.created_at").? ~
      get[LocalDateTime]("transactions.updated_at").?
  ) map { case id ~ cartId ~ cartPrice ~ deliveryPrice ~ totalPrice ~ createdAt ~ updatedAt =>
    Transaction(id, cartId, cartPrice, deliveryPrice, totalPrice, createdAt, updatedAt)
  }

  case class CreateTransactionRequest(
      cartId: Int,
      deliveryServicePrice: BigDecimal
  )

  implicit val createTransactionRequestReads: Reads[CreateTransactionRequest] = Json.reads[CreateTransactionRequest]
}
