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

  // --- NEW: Helper CRUD Umum untuk Entitas Standar ---
  // Catatan: Tipe [E] di sini adalah tipe entitas spesifik (misal: MerchType, Merchandise)
  // Ini memerlukan parser.type di BaseRepository atau passing parser eksplisit

  /** Mengambil entitas berdasarkan ID. Membutuhkan parser eksplisit karena BaseRepository tidak tahu tipe T
    * @param id
    *   ID entitas.
    * @param specificParser
    *   Parser spesifik untuk entitas T.
    * @tparam E
    *   Tipe entitas (misal: MerchType).
    * @return
    *   Future[Option[E]]
    */
  def findById[E](id: Int)(implicit specificParser: RowParser[E], connection: java.sql.Connection): Option[E] = {
    executeSingle[E](
      s"SELECT * FROM $tableName WHERE id = {id} AND is_delete = false",
      "id" -> id
    )
  }

  /** Mengambil semua entitas. Membutuhkan parser eksplisit.
    * @param specificParser
    *   Parser spesifik untuk entitas T.
    * @tparam E
    *   Tipe entitas.
    * @return
    *   Future[Seq[E]]
    */
  def findAll[E](implicit specificParser: RowParser[E], connection: java.sql.Connection): Seq[E] = {
    executeList[E](s"SELECT * FROM $tableName WHERE is_delete = false")
  }

  /** Melakukan soft delete (mengubah is_delete menjadi true). Asumsi tabel memiliki kolom 'is_delete' dan 'updated_at'.
    * @param id
    *   ID entitas yang akan di-soft delete.
    * @return
    *   Future[Boolean] true jika berhasil, false jika tidak.
    */
  def softDelete(id: Int)(implicit connection: java.sql.Connection): Boolean = {
    executeUpdate(
      s"UPDATE $tableName SET is_delete = true, updated_at = NOW() WHERE id = {id} AND is_delete = false",
      "id" -> id
    ) > 0
  }

  /** Menghapus entitas secara permanen dari database.
    * @param id
    *   ID entitas yang akan dihapus.
    * @return
    *   Future[Int] jumlah baris yang terpengaruh.
    */
  def delete(id: Int)(implicit connection: java.sql.Connection): Int = {
    executeUpdate(
      s"DELETE FROM $tableName WHERE id = {id}",
      "id" -> id
    )
  }
}
