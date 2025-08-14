package repositories

import anorm.*
import models.User
import repositories.base.BaseRepositoryNew

import java.sql.Connection
import javax.inject.*
import scala.concurrent.ExecutionContext

@Singleton
class UserRepository @Inject() ()(implicit ec: ExecutionContext) extends BaseRepositoryNew() {
  override protected val tableName: String = "users"

  def create(user: User.UserRequest)(implicit connection: Connection): User = {
    val resultId = executeInsert(
      s"INSERT INTO $tableName(name, email, city_id, address, created_at, updated_at) VALUES ({name}, {email}, {cityId}, {address}, NOW(), NOW())",
      "name"    -> user.name,
      "email"   -> user.email,
      "cityId"  -> user.cityId,
      "address" -> user.address
    )

    User(
      id = resultId.get,
      name = user.name,
      email = user.email,
      cityId = user.cityId,
      address = user.address,
      createdAt = None,
      updatedAt = None
    )
  }

  def findById(id: Int)(implicit connection: Connection): Option[User] = {
    super.findById[User](id)(User.parser)
  }

  def findByEmail(email: String)(implicit connection: Connection): Option[User] = {
    executeSingle[User](s"SELECT * FROM $tableName WHERE email = {email} AND is_delete = FALSE", "email" -> email)(
      User.parser
    )
  }

  def findAll()(implicit connection: Connection): Seq[User] = {
    super.findAll[User](User.parser)
  }

  def update(user: User)(implicit connection: Connection): Option[User] = {
    val affectedRows = executeUpdate(
      s"""
        |UPDATE $tableName
        |SET name = {name}, email = {email}, city_id = {cityId}, address = {address}, updated_at = NOW()
        |WHERE id = {id}
        |""".stripMargin,
      "id"      -> user.id,
      "name"    -> user.name,
      "email"   -> user.email,
      "cityId"  -> user.cityId,
      "address" -> user.address
    )

    if (affectedRows > 0) findById(user.id) else None
  }

  def deleteUser(id: Int)(implicit connection: Connection): Int = {
    super.delete(id)
  }

  def softDeleteUser(id: Int)(implicit connection: Connection): Boolean = {
    super.softDelete(id)
  }
}
