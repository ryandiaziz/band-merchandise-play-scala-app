package repositories.base

import anorm.*

import javax.inject.*

abstract class BaseRepositoryNew @Inject() () {
  protected val tableName: String

  def executeSingle[T](query: String, params: NamedParameter*)(implicit
      parser: RowParser[T],
      connection: java.sql.Connection
  ): Option[T] = {
    SQL(query)
      .on(params: _*)
      .as(parser.singleOpt)
  }

  def executeList[T](query: String, params: NamedParameter*)(implicit
      parser: RowParser[T],
      connection: java.sql.Connection
  ): Seq[T] = {
    SQL(query)
      .on(params: _*)
      .as(parser.*)
  }

  // --- Helper untuk operasi INSERT ---
  protected def executeInsert(query: String, params: NamedParameter*)(implicit
      connection: java.sql.Connection
  ): Option[Int] = {
    SQL(query)
      .on(params: _*)
      .executeInsert(SqlParser.scalar[Long].singleOpt)
      .map(_.toInt)
  }

  protected def executeUpdate(query: String, params: NamedParameter*)(implicit connection: java.sql.Connection): Int = {
    SQL(query)
      .on(params: _*)
      .executeUpdate()
  }

  def findById[E](id: Int)(implicit specificParser: RowParser[E], connection: java.sql.Connection): Option[E] = {
    executeSingle[E](
      s"SELECT * FROM $tableName WHERE id = {id} AND is_delete = false",
      "id" -> id
    )
  }

  def findAll[E](implicit specificParser: RowParser[E], connection: java.sql.Connection): Seq[E] = {
    executeList[E](s"SELECT * FROM $tableName WHERE is_delete = false")
  }

  def softDelete(id: Int)(implicit connection: java.sql.Connection): Boolean = {
    executeUpdate(
      s"UPDATE $tableName SET is_delete = true, updated_at = NOW() WHERE id = {id} AND is_delete = false",
      "id" -> id
    ) > 0
  }

  def delete(id: Int)(implicit connection: java.sql.Connection): Int = {
    executeUpdate(
      s"DELETE FROM $tableName WHERE id = {id}",
      "id" -> id
    )
  }
}
