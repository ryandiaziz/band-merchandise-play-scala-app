package repositories.queries

import anorm.*
import play.api.db.Database

trait BaseRepositoryQuery[T] {
  val tableName: String

  def parser: RowParser[T]

  /** Ambil semua data dengan pagination */
  def selectAll(limit: Int = 10, offset: Int = 0)(implicit conn: java.sql.Connection): List[T] = {
    SQL"""
          SELECT * FROM #$tableName
          WHERE is_delete = false
          LIMIT $limit OFFSET $offset
        """.as(parser.*)
  }

  /** Ambil data berdasarkan ID */
  def selectById(id: Int)(implicit conn: java.sql.Connection): Option[T] = {
    SQL"""
          SELECT * FROM #$tableName
          WHERE id = $id AND is_delete = false
        """.as(parser.singleOpt)
  }

  /** Soft delete */
  def softDelete(id: Int)(implicit conn: java.sql.Connection): Boolean = {
    SQL"""
          UPDATE #$tableName
          SET is_delete = true
          WHERE id = $id
        """.executeUpdate() > 0
  }

  // Create dan update akan didefinisikan di subclass, karena field tiap tabel beda
}
