package repositories

import javax.inject.*
import play.api.db.*
import anorm.*
import models.User

import scala.concurrent.{ExecutionContext, Future}
import repositories.base.BaseRepository

@Singleton
class UserRepository @Inject() (db: Database)(implicit ec: ExecutionContext) extends BaseRepository(db) {
  override protected val tableName: String = "users"

  def create(user: User.UserRequest): Future[User] = {
    executeInsert(
      s"INSERT INTO $tableName(name, email, city_id, address, created_at, updated_at) VALUES ({name}, {email}, {cityId}, {address}, NOW(), NOW())",
      "name"    -> user.name,
      "email"   -> user.email,
      "cityId"  -> user.cityId,
      "address" -> user.address
    ).map { idOpt =>
      User(
        id = idOpt.get,
        name = user.name,
        email = user.email,
        cityId = user.cityId,
        address = user.address,
        createdAt = None,
        updatedAt = None
      )
    }
  }

  def findById(id: Int): Future[Option[User]] = {
    super.findById[User](id)(User.parser)
  }

//  def findByEmail(email: String): Future[Option[User]] = {
//    executeSingle[User](s"SELECT * FROM $tableName WHERE email = {email} AND is_delete = FALSE", "email" -> email)(
//      User.parser
//    )
//  }

  def findAll(): Future[Seq[User]] = {
    super.findAll[User](User.parser)
  }

  def update(user: User): Future[Option[User]] = {
    executeUpdate(
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
    ).flatMap { affectedRows =>
      if (affectedRows > 0) findById(user.id) else Future.successful(None)
    }
  }

  def deleteUser(id: Int): Future[Int] = {
    super.delete(id)
  }

  def softDeleteUser(id: Int): Future[Boolean] = {
    super.softDelete(id)
  }
}
