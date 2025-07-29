package services

import models.*
import repositories.{CartRepository, MerchandiseRepository, UserRepository}

import java.time.LocalDateTime
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartService @Inject() (
    cartRepo: CartRepository,
    merchandiseRepo: MerchandiseRepository,
    userRepo: UserRepository
)(implicit ec: ExecutionContext) {

  def addItemToCart(request: CartMerch.AddItemToCartRequest): Future[Either[String, Cart]] = {
    for {
      userOpt <- userRepo.findById(request.userId)
      user <- userOpt match {
        case Some(u) => Future.successful(u)
        case None    => Future.failed(new Exception(s"User with ID ${request.userId} not found."))
      }

      merchandiseOpt <- merchandiseRepo.findById(request.merchandiseId)
      merchandise <- merchandiseOpt match {
        case Some(m) => Future.successful(m)
        case None    => Future.failed(new Exception(s"Merchandise with ID ${request.merchandiseId} not found."))
      }
      _ <-
        if (merchandise.stock < request.qty)
          Future.failed(new Exception(s"Stok ${merchandise.title} tidak cukup. Tersedia: ${merchandise.stock}"))
        else Future.successful(())

      currentCartOpt <- request.cartId match {
        case Some(cartId) => cartRepo.findById(cartId)
        case None         => Future.successful(None)
      }

      cart <- currentCartOpt match {
        case Some(c) =>
          if (c.userId != request.userId) Future.failed(new Exception("Cart does not belong to the specified user."))
          else Future.successful(c)
        case None =>
          cartRepo.create(
            Cart(
              userId = request.userId,
              price = BigDecimal(0),
              status = "active",
              createdAt = Some(LocalDateTime.now()),
              updatedAt = Some(LocalDateTime.now())
            )
          )
      }

      _ <- merchandiseRepo.updateStock(merchandise.id, merchandise.stock - request.qty).map {
        case Some(_) => ()
        case None    => throw new Exception(s"Failed to update stock for merchandise ID ${merchandise.id}")
      }

      existingCartMerchOpt <- cartRepo.findCartMerchByCartIdAndMerchandiseId(cart.id, merchandise.id)
      updatedOrNewCartMerch <- existingCartMerchOpt match {
        case Some(existingCartMerch) =>
          val newQty        = existingCartMerch.qty + request.qty
          val newTotalPrice = existingCartMerch.unitPrice * BigDecimal(newQty)
          cartRepo
            .updateCartMerchQty(existingCartMerch.id, newQty, newTotalPrice)
            .map(_ => existingCartMerch.copy(qty = newQty, totalPrice = newTotalPrice))
        case None =>
          val unitPrice      = merchandise.price
          val totalPriceItem = unitPrice * BigDecimal(request.qty)
          val newCartMerch = CartMerch(
            cartId = cart.id,
            merchandiseId = merchandise.id,
            qty = request.qty,
            unitPrice = unitPrice,
            totalPrice = totalPriceItem,
            createdAt = Some(LocalDateTime.now()),
            updatedAt = Some(LocalDateTime.now())
          )
          cartRepo.addMerchToCart(newCartMerch)
      }

      allCartMerchItems <- cartRepo.findCartMerchByCartId(cart.id)
      newCartTotalPrice = allCartMerchItems.map(_.totalPrice).sum
      finalCart <- cartRepo
        .update(cart.copy(price = newCartTotalPrice, updatedAt = Some(LocalDateTime.now())))
        .map(_.get)
    } yield Right(finalCart)
  }.recover { case e: Exception =>
    Left(s"Failed to add item to cart: ${e.getMessage}")
  }

  def getCart(id: Int): Future[Option[Cart]] = cartRepo.findById(id)

  def getUserActiveCart(userId: Int): Future[Option[Cart]] = {
    cartRepo.findUserActiveCart(userId)
  }

  def softDeleteCart(id: Int): Future[Boolean] = cartRepo.softDelete(id)
}
