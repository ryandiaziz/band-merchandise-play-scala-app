package models

import anorm.*
import anorm.SqlParser.*
import play.api.libs.json.{Json, OWrites, Reads}
import utils.JsonConfig
import java.time.LocalDateTime

case class User(
    id: Int = -1,
    name: String,
    email: String,
    cityId: Option[Int],
    address: Option[String],
    createdAt: Option[LocalDateTime] = None,
    updatedAt: Option[LocalDateTime] = None
)
object User {
  implicit val userReads: Reads[User] = Json.reads[User]
  implicit val userWrites: OWrites[User] = OWrites[User] { user =>
    Json.obj(
      "id"      -> user.id,
      "name"    -> user.name,
      "email"   -> user.email,
      "city_id" -> user.cityId,
      "address" -> user.address,
      JsonConfig.optionalField("created_at", user.createdAt.map(_.toString)),
      JsonConfig.optionalField("updated_at", user.updatedAt.map(_.toString))
    )
  }
  val parser: RowParser[User] = (
    int("id") ~
      str("name") ~
      str("email") ~
      int("city_id").? ~
      str("address").? ~
      get[LocalDateTime]("created_at").? ~
      get[LocalDateTime]("updated_at").?
  ) map { case id ~ name ~ email ~ cityId ~ address ~ createdAt ~ updatedAt =>
    User(id, name, email, cityId, address, createdAt, updatedAt)
  }

  case class UserRequest(
      name: String,
      email: String,
      cityId: Option[Int],
      address: Option[String]
  )

  implicit val userRequestReads: Reads[UserRequest] = Json.reads[UserRequest]
}
