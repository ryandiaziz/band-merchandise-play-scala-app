package repositories

import anorm.*
import models.MerchType
import play.api.db.*
import repositories.base.BaseRepositoryNew

import java.sql.Connection
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MerchTypeRepository @Inject() ()(implicit ec: ExecutionContext) extends BaseRepositoryNew() {
  override protected val tableName: String = "merch_type"

  def create(merchType: MerchType)(implicit connection: Connection): MerchType = {
    val resultId = executeInsert(
      s"INSERT INTO $tableName(name, description) VALUES ({name}, {description})",
      "name"        -> merchType.name,
      "description" -> merchType.description
    )

    merchType.copy(id = resultId.get)
  }

  def findById(id: Int)(implicit connection: Connection): Option[MerchType] = {
    super.findById[MerchType](id)(MerchType.parser)
  }

  def findAll()(implicit connection: Connection): Seq[MerchType] = {
    super.findAll[MerchType](MerchType.parser)
  }

  def update(merchType: MerchType)(implicit connection: Connection): Option[MerchType] = {
    val affectedRows = executeUpdate(
      s"UPDATE $tableName SET name = {name}, description = {description}, updated_at = NOW() WHERE id = {id}",
      "id"          -> merchType.id,
      "name"        -> merchType.name,
      "description" -> merchType.description
    )

    if (affectedRows > 0) findById(merchType.id) else None
  }

  def deleteMerchType(id: Int)(implicit connection: Connection): Int = {
    super.delete(id)
  }

  def softDeleteMerchType(id: Int)(implicit connection: Connection): Boolean = {
    super.softDelete(id)
  }
}
