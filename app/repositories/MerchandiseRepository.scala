package repositories

import javax.inject._
import play.api.db._
import anorm._
import models.Merchandise
import scala.concurrent.{Future, ExecutionContext}
import repositories.base.BaseRepository

@Singleton
class MerchandiseRepository @Inject() (db: Database)(implicit ec: ExecutionContext) extends BaseRepository(db) {
  override protected val tableName: String = "merchandise"

  def create(merchandise: Merchandise): Future[Merchandise] = {
    executeInsert(
      s"""
        |INSERT INTO $tableName(title, band_name, merch_type_id, description, price, image_url, stock, created_at, updated_at)
        |VALUES ({title}, {bandName}, {merchTypeId}, {description}, {price}, {imageUrl}, {stock}, NOW(), NOW())
        |""".stripMargin,
      "title"       -> merchandise.title,
      "bandName"    -> merchandise.bandName,
      "merchTypeId" -> merchandise.merchTypeId,
      "description" -> merchandise.description,
      "price"       -> merchandise.price,
      "imageUrl"    -> merchandise.imageUrl,
      "stock"       -> merchandise.stock
    ).map { idOpt =>
      merchandise.copy(id = idOpt.get)
    }
  }

  def findById(id: Int): Future[Option[Merchandise]] = {
    super.findById[Merchandise](id)(Merchandise.parser)
  }

  def findAll(): Future[Seq[Merchandise]] = {
    super.findAll[Merchandise](Merchandise.parser)
  }

  def update(id: Int, merchandiseReq: Merchandise.MerchandiseRequest): Future[Option[Merchandise]] = {
    executeUpdate(
      s"""
        |UPDATE $tableName
        |SET title = {title}, band_name = {bandName}, merch_type_id = {merchTypeId},
        |description = {description}, price = {price}, image_url = {imageUrl},
        |stock = {stock}, updated_at = NOW()
        |WHERE id = {id}
        |""".stripMargin,
      "id"          -> id,
      "title"       -> merchandiseReq.title,
      "bandName"    -> merchandiseReq.bandName,
      "merchTypeId" -> merchandiseReq.merchTypeId,
      "description" -> merchandiseReq.description,
      "price"       -> merchandiseReq.price,
      "imageUrl"    -> merchandiseReq.imageUrl,
      "stock"       -> merchandiseReq.stock
    ).flatMap { affectedRows =>
      if (affectedRows > 0) findById(id) else Future.successful(None)
    }
  }

  def updateStock(id: Int, newStock: Int): Future[Option[Merchandise]] = {
    executeUpdate(
      s"UPDATE $tableName SET stock = {newStock}, updated_at = NOW() WHERE id = {id}",
      "id"       -> id,
      "newStock" -> newStock
    ).flatMap { affectedRows =>
      if (affectedRows > 0) findById(id) else Future.successful(None)
    }
  }

  def deleteMerch(id: Int): Future[Int] = {
    super.delete(id)
  }

  def softDeleteMerch(id: Int): Future[Boolean] = {
    super.softDelete(id)
  }
}
