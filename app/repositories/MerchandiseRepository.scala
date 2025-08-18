package repositories

import anorm.*
import models.Merchandise
import repositories.base.BaseRepositoryNew

import java.sql.Connection
import javax.inject.*
import scala.concurrent.ExecutionContext

@Singleton
class MerchandiseRepository @Inject() ()(implicit ec: ExecutionContext) extends BaseRepositoryNew() {
  override protected val tableName: String = "merchandise"

  def create(merchandise: Merchandise)(implicit connection: Connection): Merchandise = {
    val resultId = executeInsert(
      s"""
        |INSERT INTO $tableName(title, band_name, merch_type_id, description, price, image_url, stock, created_at, updated_at)
        |VALUES ({title}, {band_name}, {merchTypeId}, {description}, {price}, {image_url}, {stock}, NOW(), NOW())
        |""".stripMargin,
      "title"       -> merchandise.title,
      "band_name"    -> merchandise.bandName,
      "merchTypeId" -> merchandise.merchTypeId,
      "description" -> merchandise.description,
      "price"       -> merchandise.price,
      "image_url"    -> merchandise.imageUrl,
      "stock"       -> merchandise.stock
    )

    merchandise.copy(id = resultId.get)
  }

  def findById(id: Int)(implicit connection: Connection): Option[Merchandise] = {
    super.findById[Merchandise](id)(Merchandise.parser)
  }

  def findByIdDetail(id: Int)(implicit connection: Connection): Option[Merchandise.MerchandiseWithMerchType] = {
    executeSingle[Merchandise.MerchandiseWithMerchType](
      s"""
        |SELECT m.*, mt.*
        |FROM merchandise m
        |JOIN merch_type mt ON m.merch_type_id = mt.id
        |WHERE m.id = {id} AND m.is_delete = false
        |""".stripMargin,
      "id" -> id
    )(Merchandise.merchWithMerchTypeParser)
  }

  def findAll()(implicit connection: Connection): Seq[Merchandise] = {
    super.findAll[Merchandise](Merchandise.parser)
  }

  def update(id: Int, merchandiseReq: Merchandise)(implicit
      connection: Connection
  ): Option[Merchandise] = {
    val affectedRows = executeUpdate(
      s"""
        |UPDATE $tableName
        |SET title = {title}, band_name = {band_name}, merch_type_id = {merchTypeId},
        |description = {description}, price = {price}, image_url = {image_url},
        |stock = {stock}, updated_at = NOW()
        |WHERE id = {id}
        |""".stripMargin,
      "id"          -> id,
      "title"       -> merchandiseReq.title,
      "band_name"    -> merchandiseReq.bandName,
      "merchTypeId" -> merchandiseReq.merchTypeId,
      "description" -> merchandiseReq.description,
      "price"       -> merchandiseReq.price,
      "image_url"    -> merchandiseReq.imageUrl,
      "stock"       -> merchandiseReq.stock
    )

    if (affectedRows > 0) findById(id) else None
  }

  def updateStock(id: Int, newStock: Int)(implicit connection: Connection): Option[Merchandise] = {
    val affectedRows = executeUpdate(
      s"UPDATE $tableName SET stock = {newStock}, updated_at = NOW() WHERE id = {id}",
      "id"       -> id,
      "newStock" -> newStock
    )

    if (affectedRows > 0) findById(id) else None
  }

  def deleteMerch(id: Int)(implicit connection: Connection): Int = {
    super.delete(id)
  }

  def softDeleteMerch(id: Int)(implicit connection: Connection): Boolean = {
    super.softDelete(id)
  }
}
