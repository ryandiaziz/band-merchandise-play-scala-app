package services

import models.barang.Barang

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}
import models.keranjang.{Keranjang, KeranjangAddItemRequest, KeranjangItem}
import repositories.{BarangRepository, KeranjangRepository}

@Singleton
class KeranjangService @Inject() (
    barangRepo: BarangRepository,
    keranjangRepo: KeranjangRepository
)(implicit ec: ExecutionContext) {

  // Metode utama untuk menambahkan item ke keranjang
  def tambahItemKeKeranjang(request: KeranjangAddItemRequest): Future[Either[String, Keranjang]] = {
    // 1. Cari Barang
    barangRepo.findById(request.barangId).flatMap {
      case Some(barang) =>
        // 2. Validasi Stok
        val currentStock = barang.stok.get
        if (currentStock < request.jumlah) {
          Future.successful(Left(s"Stok barang '${barang.nama}' tidak cukup. Tersedia: ${barang.stok}"))
        } else {
          // 3. Tentukan Keranjang (buat baru atau cari yang sudah ada)
          request.keranjangId match {
            case Some(existingKeranjangId) =>
              keranjangRepo.findById(existingKeranjangId.toInt).flatMap {
                case Some(keranjang) =>
                  // Keranjang ditemukan, lanjutkan dengan keranjang yang ada
                  prosesPenambahanItem(keranjang, barang, request.jumlah)
                case None =>
                  // Keranjang tidak ditemukan, kembalikan error
                  Future.successful(Left(s"Keranjang dengan ID ${existingKeranjangId} tidak ditemukan."))
              }
            case None =>
              // Tidak ada keranjang_id, buat keranjang baru
              val newKeranjang = Keranjang(userId = request.userId, totalHarga = 0)
              keranjangRepo.create(newKeranjang).flatMap { createdKeranjang =>
                prosesPenambahanItem(createdKeranjang, barang, request.jumlah)
              }
          }
        }
      case None =>
        // Barang tidak ditemukan
        Future.successful(Left(s"Barang dengan ID ${request.barangId} tidak ditemukan."))
    }
  }

  // Logika internal untuk mengurangi stok, menambahkan item, dan update total harga keranjang
  private def prosesPenambahanItem(
      keranjang: Keranjang,
      barang: Barang,
      jumlah: Int
  ): Future[Either[String, Keranjang]] = {
    for {
      // 1. Kurangi stok barang
      // updateStok sekarang mengembalikan Future[Option[Barang]]
      updatedBarangOpt <- barangRepo.updateStok(barang.id, barang.stok.get.toInt - jumlah)
      // Pastikan stok berhasil diupdate, jika tidak, gagal
      _ <- updatedBarangOpt match {
        case Some(_) => Future.successful(()) // Stok berhasil diupdate
        case None =>
          Future.failed(new Exception("Gagal update stok barang, barang tidak ditemukan setelah validasi awal."))
      }

      // 2. Hitung detail item keranjang
      unitHarga      = barang.harga.get
      totalHargaItem = unitHarga * jumlah

      // 3. Tambahkan item ke keranjang
      newItem = KeranjangItem(
        keranjangId = keranjang.keranjangId, // Pasti ada karena keranjang sudah dibuat/ditemukan
        barangId = barang.id,
        jumlah = jumlah,
        unitHarga = unitHarga,
        totalHargaItem = totalHargaItem
      )
      _ <- keranjangRepo.addItem(newItem)

      // 4. Perbarui total harga keranjang
      currentItems <- keranjangRepo.findItemsByKeranjangId(keranjang.keranjangId)
      newTotalHargaKeranjang = currentItems.map(_.totalHargaItem).sum // Hitung ulang total dari semua item
      updatedKeranjang       = keranjang.copy(totalHarga = newTotalHargaKeranjang)
      finalKeranjang <- keranjangRepo.update(updatedKeranjang)
    } yield Right(finalKeranjang) // Kembalikan keranjang yang sudah diupdate
  }.recover {                     // Menangani semua kegagalan di for-comprehension
    case e: Exception => Left(s"Terjadi kesalahan saat memproses item keranjang: ${e.getMessage}")
  }
}
