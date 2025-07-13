package models.user

case class User(
    userId: Int = -1,
    username: String,
    photo: Option[String]
)
