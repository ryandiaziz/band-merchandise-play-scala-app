package repositories

import anorm.*
import models.kategori.Kategori
import play.api.db.DBApi

import javax.inject.Inject
import org.postgresql.util.PSQLException

@javax.inject.Singleton
class KategoriRepository @Inject() (dbapi: DBApi) {

  private val db        = dbapi.database("default")
  private val tableName = "kategori"

  def create(kategori: Kategori): Option[Long] = db.withConnection(implicit connection => {
    var id: Option[Long] = None
    val query: String =
      s"""
          | INSERT INTO kategori (nama)
          | VALUES ({nama})
          |""".stripMargin

    try {
      id = SQL(query)
        .on(
          "nama" -> kategori.nama
        )
        .executeInsert(SqlParser.scalar[Long].singleOpt)
    } catch {
      case e: PSQLException => id = None
    }
    id
  })

  def all(): List[Kategori] = db.withConnection { implicit connection =>
    SQL"""
            SELECT * FROM kategori WHERE is_delete = false
            """.as(Kategori.parser.*)
  }

  def findById(id: Int): Option[Kategori] = db.withConnection { implicit connection =>
    val stmQuery =
      s"""
        |SELECT
        |kategori.kategori_id, kategori.nama, kategori.is_delete
        |FROM $tableName
        |WHERE kategori.id = $id AND kategori.is_delete = false
        |""".stripMargin

    SQL(stmQuery).as(Kategori.parser.singleOpt)
    //        SQL(s"SELECT * FROM $tableName WHERE id = 2 AND is_delete = false").as(barangParser.singleOpt)
  }

  def update(kategori: Kategori): Option[Long] = db.withConnection { implicit connection =>
    val stmQuery: String =
      s"""
        |UPDATE kategori SET
        |nama = {nama}
        |WHERE id = {id}
        |""".stripMargin

    try {
      val updatedCount: Int = SQL(stmQuery)
        .on(
          "nama" -> kategori.nama,
          "id"   -> kategori.id
        )
        .executeUpdate()

      if (updatedCount > 0) Some(kategori.id.toLong) else None

    } catch {
      case e: Exception =>
        // log error kalau perlu
        throw Exception(e.getMessage)
    }
  }

  def softDelete(id: Int): Int = db.withConnection { implicit connection =>
    val query: String = s"UPDATE kategori SET is_delete = true WHERE id = {id}"

    SQL(query).on(
      "id" -> id
    ).executeUpdate()
  }
}
