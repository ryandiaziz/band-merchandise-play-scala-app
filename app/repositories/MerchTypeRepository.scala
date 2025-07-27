package repositories

import javax.inject._
import play.api.db._ // Tetap diimport karena diinject ke BaseRepository
import anorm._
import models.MerchType
import scala.concurrent.{Future, ExecutionContext}
import repositories.base.BaseRepository

@Singleton
class MerchTypeRepository @Inject() (db: Database)(implicit ec: ExecutionContext) extends BaseRepository(db) {

  // --- Implementasi properti abstrak dari BaseRepository ---
  // Kita harus mendefinisikan nama tabel yang akan digunakan oleh helper
  override protected val tableName: String = "merch_type"

  // --- Metode CREATE (Tetap spesifik karena INSERT memerlukan daftar kolom) ---
  def create(merchType: MerchType): Future[MerchType] = {
    executeInsert(
      s"INSERT INTO $tableName(name, description) VALUES ({name}, {description})",
      "name"        -> merchType.name,
      "description" -> merchType.description
    ).map { idOpt =>
      // Setelah insert, kembalikan objek dengan ID yang sudah di-generate
      merchType.copy(id = idOpt.get)
    }
  }

  // --- Metode CRUD yang Sekarang Menggunakan Helper dari BaseRepository ---

  // GET by ID: Langsung panggil helper findById dari BaseRepository
  def findById(id: Int): Future[Option[MerchType]] = {
    // Kita meneruskan MerchType.parser secara eksplisit ke helper findById
    super.findById[MerchType](id)(MerchType.parser)
  }

  // GET ALL: Langsung panggil helper findAll dari BaseRepository
  def findAll(): Future[Seq[MerchType]] = {
    // Kita meneruskan MerchType.parser secara eksplisit ke helper findAll
    super.findAll[MerchType](MerchType.parser)
  }

  // UPDATE: (Tetap spesifik karena UPDATE SET memerlukan daftar kolom yang spesifik)
  def update(merchType: MerchType): Future[Option[MerchType]] = {
    executeUpdate(
      s"UPDATE $tableName SET name = {name}, description = {description}, updated_at = NOW() WHERE id = {id}",
      "id"          -> merchType.id, // Asumsi ID selalu ada untuk update
      "name"        -> merchType.name,
      "description" -> merchType.description
    ).flatMap { affectedRows =>
      // Setelah update, kita fetch ulang data terbaru untuk memastikan konsistensi
      if (affectedRows > 0) findById(merchType.id) else Future.successful(None)
    }
  }

  // DELETE (Hard Delete): Langsung panggil helper delete dari BaseRepository
  def deleteMerchType(id: Int): Future[Int] = {
    super.delete(id)
  }

  // SOFT DELETE: Langsung panggil helper softDelete dari BaseRepository
  // Asumsi tabel `merch_type` memiliki kolom `is_delete` (sesuai evolusi yang kita buat)
  def softDeleteMerchType(id: Int): Future[Boolean] = {
    super.softDelete(id)
  }
}
