package repositories

import anorm.*
import models.barang.{Barang, BarangWithKategori}
import models.user.User
import play.api.db.DBApi

import javax.inject.Inject
import org.postgresql.util.PSQLException
import utils.Helper

class UserRepository @Inject() (dbapi: DBApi) {
  private val db = dbapi.database("default")

  def create(user: User): Option[Long] = db.withConnection(implicit connection => {
    var id: Option[Long] = None

    val stmQuery: String =
      s"""
        | INSERT INTO public.users (username, photo)
        | VALUES ({username}, {photo})
        |""".stripMargin

    try {
      id = SQL(stmQuery)
        .on(
          "username" -> user.username,
          "photo"    -> user.photo.get
        )
        .executeInsert(SqlParser.scalar[Long].singleOpt)
    } catch {
      case e: PSQLException =>
        println(e.getMessage)
        throw new Exception(e.getMessage)
        id = None
    }

    id
  })

  case class UserParser(
      user_id: Int,
      username: String,
      photo: Option[String]
  )
  
  val parser: RowParser[UserParser] = Macro.namedParser[UserParser]

  def getUser(id: Long): (Option[User], String) = db.withConnection { implicit c =>
    val query = SQL("""
        |SElECT *
        |FROM public.users
        |WHERE user_id = {id}
        |""".stripMargin)
      .on(
        "id" -> id
      )
      .as(parser.singleOpt)
    
    val user = User(
      userId = query.get.user_id,
      username = query.get.username,
      photo = query.get.photo
      
    )
    
    (Some(user), "berhasil")
  }
}
