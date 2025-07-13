package repositories

import javax.inject.*
import play.api.db.*
import anorm.*
import models.keranjang.{Keranjang, KeranjangItem, KeranjangWithItem}
import repositories.queries.KeranjangQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future

class KeranjangRepository @Inject() (db: Database, barangRepo: BarangRepository)(implicit ec: ExecutionContext) {
  private val query                  = new KeranjangQuery
  private val keranjangTableName     = "keranjang"
  private val keranjangItemTableName = "item_keranjang"

  def create(keranjang: Keranjang): Future[Keranjang] = Future {
    db.withConnection { implicit connection =>
      val id: Option[Long] = SQL(
        s"""
          |INSERT INTO $keranjangTableName(user_id, total_harga)
          |VALUES ({userId}, {totalHarga})
          |""".stripMargin
      ).on(
        "userId"     -> keranjang.userId,
        "totalHarga" -> keranjang.totalHarga
      ).executeInsert(SqlParser.scalar[Long].singleOpt) // Mengambil ID yang di-generate

      keranjang.copy(keranjangId = id.map(_.toInt).get) // Mengembalikan objek Keranjang dengan ID yang ter-generate
    }
  }

  def findById(id: Int): Future[Option[Keranjang]] = Future {
    db.withConnection { implicit connection =>
      SQL(s"SELECT id, user_id, total_harga FROM $keranjangTableName WHERE id = {id}")
        .on("id" -> id)
        .as(Keranjang.parser.singleOpt)
    }
  }

  def update(keranjang: Keranjang): Future[Keranjang] = Future {
    db.withConnection { implicit connection =>
      SQL(
        s"""
          |UPDATE $keranjangTableName
          |SET total_harga = {totalHarga}
          |WHERE id = {id}
          |""".stripMargin
      ).on(
        "totalHarga" -> keranjang.totalHarga,
        "id"         -> keranjang.keranjangId
      ).executeUpdate()

      keranjang // Mengembalikan objek yang diupdate (atau bisa findById lagi untuk memastikan)
    }
  }

  def addItem(keranjangItem: KeranjangItem): Future[KeranjangItem] = Future {
    db.withConnection { implicit connection =>
      val id: Option[Long] = SQL(
        s"""
          |INSERT INTO $keranjangItemTableName(keranjang_id, barang_id, jumlah, unit_harga, total_harga_item)
          |VALUES ({keranjangId}, {barangId}, {jumlah}, {unitHarga}, {totalHargaItem})
          |""".stripMargin
      ).on(
        "keranjangId"    -> keranjangItem.keranjangId,
        "barangId"       -> keranjangItem.barangId,
        "jumlah"         -> keranjangItem.jumlah,
        "unitHarga"      -> keranjangItem.unitHarga,
        "totalHargaItem" -> keranjangItem.totalHargaItem
      ).executeInsert(SqlParser.scalar[Long].singleOpt)

      keranjangItem.copy(keranjangItemId = id.map(_.toInt).get)
    }
  }

  def findItemsByKeranjangId(keranjangId: Int): Future[List[KeranjangItem]] = Future {
    db.withConnection { implicit connection =>
      SQL(
        s"SELECT id, keranjang_id, barang_id, jumlah, unit_harga, total_harga_item FROM $keranjangItemTableName WHERE keranjang_id = {keranjangId}"
      )
        .on("keranjangId" -> keranjangId)
        .as(KeranjangItem.parser.*) // Menggunakan parser KeranjangItem dan mengharapkan banyak hasil
    }
  }
}
