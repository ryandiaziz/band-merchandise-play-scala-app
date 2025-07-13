package repositories

import anorm.*
import models.barang.{Barang, BarangWithKategori}
import play.api.db.DBApi

import javax.inject.Inject
import org.postgresql.util.PSQLException
import utils.Helper

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class BarangRepository @Inject() (dbapi: DBApi)(implicit ec: ExecutionContext) {
  private val db        = dbapi.database("default")
  private val tableName = "barang"

  def all(page: Int): List[BarangWithKategori] = db.withConnection { implicit connection =>
    val startRow   = Helper.start(page)
    val limitation = if page > 0 then s" LIMIT ${Helper.limit} OFFSET $startRow" else ""
    val queryWhere = ""

    val query =
      """
      |SELECT
      |barang.barang_id, barang.nama, barang.kategori_id, barang.harga, barang.stok, barang.is_delete,
      |kategori.kategori_id, kategori.nama, kategori.is_delete
      |FROM barang
      |JOIN kategori ON barang.kategori_id = kategori.kategori_id
      |WHERE barang.is_delete = false AND kategori.is_delete = false
      |""".stripMargin

    SQL(query + limitation).as(BarangWithKategori.parser.*)
  }

  def create(barang: Barang): Option[Long] = db.withConnection(implicit connection => {
    var id: Option[Long] = None

    val stmQuery: String =
      s"""
        | INSERT INTO barang (nama, kategori_id, harga, stok)
        | VALUES ({nama}, {kategoriId}, {harga}, {stok})
        |""".stripMargin

    try {
      id = SQL(stmQuery)
        .on(
          "nama"       -> barang.nama,
          "kategoriId" -> barang.kategoriId,
          "harga"      -> barang.harga.getOrElse(0.0),
          "stok"       -> barang.stok.getOrElse(0L)
        )
        .executeInsert(SqlParser.scalar[Long].singleOpt)
    } catch {
      case e: PSQLException => id = None
    }

    id
  })

  // --- Implementasi findById dengan Anorm ---
  def findById(id: Int): Future[Option[Barang]] = Future { // Mengembalikan Future[Option[Barang]]
    db.withConnection { implicit connection => // Mendapatkan koneksi dari pool Play
      val stmQuery =
        s"""
          |SELECT
          |barang_id, nama, kategori_id, harga, stok, is_delete
          |FROM $tableName
          |WHERE barang_id = {id} AND is_delete = false
          |""".stripMargin

      SQL(stmQuery)
        .on("id" -> id) // Menggunakan parameterisasi untuk mencegah SQL Injection
        .as(Barang.parser.singleOpt) // Menggunakan parser Barang dan mengharapkan 0 atau 1 hasil
    }
  }

  // --- Implementasi updateStok dengan Anorm ---
  def updateStok(id: Int, newStok: Int): Future[Option[Barang]] = Future {
    db.withConnection { implicit connection =>
      val updatedRows = SQL(
        s"""
          |UPDATE $tableName
          |SET stok = {newStok}
          |WHERE barang_id = {id} AND is_delete = false""".stripMargin
      ).on(
        "newStok" -> newStok,
        "id" -> id
      ).executeUpdate()

      // Setelah update, kita perlu mengambil data barang terbaru secara ASINKRON
      // Gunakan Future.successful(None) jika tidak ada baris yang diupdate
      if (updatedRows > 0) {
        // Jika update berhasil, cari kembali barang yang sudah diupdate
        // Kita tidak bisa langsung memanggil findById().value.get di sini
        // karena updateStok harus mengembalikan Future[Option[Barang]] secara langsung.
        // Solusinya adalah menjalankan findById dalam Future terpisah
        // atau mempercayai bahwa update berhasil dan mengembalikan barang yang diupdate.
        // Untuk kesederhanaan, mari kita kembalikan Some(Barang) dari hasil pencarian lagi
        // (ini akan memicu query DB kedua, bisa dioptimasi nanti jika perlu)
        SQL(s"SELECT barang_id, nama, kategori_id, harga, stok, is_delete FROM $tableName WHERE barang_id = {id}")
          .on("id" -> id)
          .as(Barang.parser.singleOpt)
      } else {
        None // Barang tidak ditemukan atau tidak ada yang diupdate
      }
    }
  }

  def findByIdDetail(id: Int): Option[BarangWithKategori] = db.withConnection { implicit connection =>
    val stmQuery =
      s"""
        |SELECT
        |barang.barang_id, barang.nama, barang.kategori_id, barang.harga, barang.stok, barang.is_delete,
        |kategori.kategori_id, kategori.nama, kategori.is_delete
        |FROM $tableName
        |JOIN kategori ON barang.kategori_id = kategori.kategori_id
        |WHERE barang.barang_id = $id AND barang.is_delete = false AND kategori.is_delete = false
        |""".stripMargin

    SQL(stmQuery).as(BarangWithKategori.parser.singleOpt)
  }

  def update(barang: Barang): Option[Long] = db.withConnection { implicit connection =>
    val stmQuery: String =
      s"""
        |UPDATE barang SET
        |nama = {nama},
        |kategori_id = {kategoriId},
        |harga = {harga},
        |stok = {stok}
        |WHERE id = {id}
        |""".stripMargin

    try {
      val updatedCount: Int = SQL(stmQuery)
        .on(
          "nama"       -> barang.nama,
          "kategoriId" -> barang.kategoriId,
          "harga"      -> barang.harga.getOrElse(0.0),
          "stok"       -> barang.stok.getOrElse(0L),
          "id"         -> barang.id
        )
        .executeUpdate()

      if (updatedCount > 0) Some(barang.id.toLong) else None

    } catch {
      case e: Exception =>
        throw Exception(e.getMessage)
    }
  }

  def softDelete(id: Int): Int = db.withConnection { implicit connection =>
    val query: String = s"UPDATE barang SET is_delete = true WHERE id = {id}"

    SQL(query)
      .on(
        "id" -> id
      )
      .executeUpdate()
  }
}
