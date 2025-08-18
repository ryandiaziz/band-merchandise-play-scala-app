package services

import models.*
import play.api.db.Database
import repositories.{CartRepository, MerchandiseRepository, UserRepository}

import java.time.LocalDateTime
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartService @Inject() (
    db: Database,
    cartRepo: CartRepository,
    merchandiseRepo: MerchandiseRepository,
    userRepo: UserRepository
)(implicit ec: ExecutionContext) {

  def addItemToCart(request: CartMerch.AddItemToCartRequest): Future[Option[Cart]] = Future {
    db.withTransaction { implicit connection =>
      val userOpt = userRepo.findById(request.userId)
      val user    = userOpt.getOrElse(throw new Exception(s"User with ID ${request.userId} not found."))

      val merchandiseOpt = merchandiseRepo.findById(request.merchandiseId)
      val merchandise =
        merchandiseOpt.getOrElse(throw new Exception(s"Merchandise with ID ${request.merchandiseId} not found."))

      if (merchandise.stock < request.qty) {
        throw new Exception(s"Stok ${merchandise.title} tidak cukup. Tersedia: ${merchandise.stock}")
      }

      val currentCartOpt = request.cartId match {
        case Some(cartId) => cartRepo.findById(cartId)
        case None         => cartRepo.findUserActiveCart(request.userId)
      }

      val cart = currentCartOpt match {
        case Some(c) => {
          if (c.userId != request.userId) throw new Exception("Cart does not belong to the specified user.")
          else c
        }
        case None => {
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
      }

      // --- Operasi Tulis di Sini ---
      val updatedStock = merchandise.stock - request.qty
      merchandiseRepo.updateStock(merchandise.id, updatedStock)

      val existingCartMerchOpt = cartRepo.findCartMerchByCartIdAndMerchandiseId(cart.id, merchandise.id)
      existingCartMerchOpt match {
        case Some(existingCartMerch) => {
          val newQty = existingCartMerch.qty + request.qty
          val newTotalPrice = existingCartMerch.unitPrice * BigDecimal(newQty)
          cartRepo.updateCartMerchQty(existingCartMerch.id, newQty, newTotalPrice)
        }
        case None => {
          val unitPrice = merchandise.price
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
      }

      val allCartMerchItems = cartRepo.findCartMerchByCartId(cart.id)
      val newCartTotalPrice = allCartMerchItems.map(_.totalPrice).sum
      val finalCart = cartRepo.update(cart.copy(price = newCartTotalPrice, updatedAt = Some(LocalDateTime.now())))

      finalCart // Hasil akhir
    }
  }
  
  def getCart(id: Int): Future[Option[Cart]] = Future {
    db.withConnection { implicit connection =>
      cartRepo.findById(id)
    }
  }

  def getUserActiveCart(userId: Int): Future[Option[Cart]] = Future {
    db.withConnection { implicit connection =>
      cartRepo.findUserActiveCart(userId)
    }
  }

  def softDeleteCart(id: Int): Future[Boolean] = Future {
    db.withTransaction { implicit connection =>
      cartRepo.softDelete(id)
    }
  }
}
