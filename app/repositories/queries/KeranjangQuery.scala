package repositories.queries

import anorm.*
import anorm.SqlParser.*
import models.keranjang.{KeranjangItem, Keranjang}
import org.postgresql.util.PSQLException

import java.sql.Connection

class KeranjangQuery {
  val tableName: String = "keranjang"

  case class FlatKeranjangRow(
      keranjangId: Int,
      userId: Int,
      totalHargaKeranjang: Double,
      item: KeranjangItem
  )

//  val flatRowParser: RowParser[FlatKeranjangRow] = {
//    get[Int]("keranjang_id") ~
//      get[Int]("user_id") ~
//      get[Double]("total_harga_keranjang") ~
//      get[Int]("barang_id") ~
//      get[String]("nama_barang") ~
//      get[Double]("harga") ~
//      get[Int]("stok") ~
//      get[Int]("kategori_id") ~
//      get[Double]("jumlah") ~
//      get[Double]("unit_harga") ~
//      get[Double]("total_harga_item") ~
//      get[Int]("kategori.kategori_id") ~
//      get[String]("nama_kategori") map {
//        case kid ~ uid ~ total ~ bid ~ bnama ~ bharga ~ bstok ~ bkatid ~ jumlah ~ uharga ~ totalItem ~ katid ~ katnama =>
//          FlatKeranjangRow(
//            keranjangId = kid,
//            userId = uid,
//            totalHargaKeranjang = total,
//            item = KeranjangItem(
//              barangId = bid,
//              namaBarang = bnama,
//              hargaBarang = bharga,
//              stokBarang = bstok,
//              kategoriIdBarang = bkatid,
//              jumlah = jumlah,
//              unitHarga = uharga,
//              totalHargaItem = totalItem,
//              kategoriId = katid,
//              namaKategori = katnama
//            )
//          )
//      }
//  }

//  def selectAll()(implicit c: Connection): List[FlatKeranjangRow] = {
//    val query =
//      """
//        |SELECT
//        |  keranjang.keranjang_id,
//        |  keranjang.user_id,
//        |  keranjang.total_harga AS total_harga_keranjang,
//        |  b.barang_id,
//        |  b.nama AS nama_barang,
//        |  b.harga,
//        |  b.stok,
//        |  b.kategori_id,
//        |  ik.jumlah,
//        |  ik.unit_harga,
//        |  ik.total_harga_item,
//        |  kat.kategori_id,
//        |  kat.nama AS nama_kategori
//        |FROM keranjang
//        |JOIN item_keranjang ik ON keranjang.keranjang_id = ik.keranjang_id
//        |JOIN barang b ON ik.barang_id = b.barang_id
//        |JOIN kategori kat ON b.kategori_id = kat.kategori_id
//        |WHERE (keranjang.is_delete IS NULL OR keranjang.is_delete = false);
//        |""".stripMargin
//
//    try {
//      SQL(query).as(flatRowParser.*)
//    } catch {
//      case e: PSQLException =>
//        println("Terjadi error PSQLException:")
//        e.printStackTrace()
//        throw Exception(e.getMessage)
//      case e: Exception =>
//        println("Terjadi error Exception:")
//        throw Exception(e.getMessage)
//    }
//  }

//  def selectByUserId(userId: Int)(implicit c: Connection): List[FlatKeranjangRow] = {
//    val query =
//      """
//        |SELECT
//        |  keranjang.keranjang_id,
//        |  keranjang.user_id,
//        |  keranjang.total_harga AS total_harga_keranjang,
//        |  b.barang_id,
//        |  b.nama AS nama_barang,
//        |  b.harga,
//        |  b.stok,
//        |  b.kategori_id,
//        |  ik.jumlah,
//        |  ik.unit_harga,
//        |  ik.total_harga_item,
//        |  kat.kategori_id,
//        |  kat.nama AS nama_kategori
//        |FROM keranjang
//        |JOIN item_keranjang ik ON keranjang.keranjang_id = ik.keranjang_id
//        |JOIN barang b ON ik.barang_id = b.barang_id
//        |JOIN kategori kat ON b.kategori_id = kat.kategori_id
//        |WHERE keranjang.user_id = {user_id}
//        |AND (keranjang.is_delete IS NULL OR keranjang.is_delete = false);
//        |""".stripMargin
//
//    try {
//      SQL(query)
//        .on(
//          "user_id" -> userId
//        )
//        .as(flatRowParser.*)
//    } catch {
//      case e: PSQLException =>
//        println("Terjadi error PSQLException:")
//        e.printStackTrace()
//        throw Exception(e.getMessage)
//      case e: Exception =>
//        println("Terjadi error Exception:")
//        throw Exception(e.getMessage)
//    }
//  }

  def insert(keranjang: Keranjang)(implicit c: java.sql.Connection): Option[Long] = {
    SQL"""
      INSERT INTO keranjang (user_id, total_harga)
      VALUES (${keranjang.userId}, ${keranjang.totalHarga})
    """.executeInsert()
  }

  def update(keranjang: Keranjang)(implicit c: java.sql.Connection): Boolean = {
    SQL"""
      UPDATE keranjang
      SET user_id = ${keranjang.userId}, total_harga = ${keranjang.totalHarga}
      WHERE keranjang_id = ${keranjang.keranjangId}
    """.executeUpdate() > 0
  }
}
