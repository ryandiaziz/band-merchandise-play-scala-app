package services

import models.{MerchType, Merchandise}
import play.api.db.Database
import repositories.{MerchTypeRepository, MerchandiseRepository}

import java.sql.Connection
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MerchandiseService @Inject() (
    db: Database, // <-- PERBAIKAN: Injeksi db: Database
    merchandiseRepo: MerchandiseRepository,
    merchTypeRepo: MerchTypeRepository
)(implicit ec: ExecutionContext) {

  // --- createMerchandise: Menggunakan transaksi untuk atomicity ---
  def createMerchandise(request: Merchandise.MerchandiseRequest): Future[Either[String, Merchandise]] = Future {
    db.withTransaction { implicit connection =>
      // Ini adalah kode SINKRON di dalam Future
      val merchType = merchTypeRepo.findById(request.merchTypeId)
      merchType match {
        case Some(_) =>
          val newMerch = Merchandise(
            title = request.title,
            bandName = request.bandName,
            merchTypeId = request.merchTypeId,
            description = request.description,
            price = request.price,
            imageUrl = request.imageUrl,
            stock = request.stock
          )
          val createdMerch = merchandiseRepo.create(newMerch)
          Right(createdMerch)
        case None =>
          Left(s"MerchType ID ${request.merchTypeId} not found.")
      }
    }
  }

  // --- updateMerchType: Menggunakan transaksi untuk atomicity ---
  def updateMerchType(id: Int, request: Merchandise.MerchandiseRequest): Future[Option[Merchandise]] = Future {
    db.withTransaction { implicit connection =>
      val merchType = merchTypeRepo.findById(request.merchTypeId)
      merchType match {
        case Some(_) =>
          val merchandise = merchandiseRepo.findById(id)
          merchandise match {
            case Some(existingMerch) =>
              val updatedMerch = existingMerch.copy(
                title = request.title,
                bandName = request.bandName,
                merchTypeId = request.merchTypeId,
                description = request.description,
                price = request.price,
                imageUrl = request.imageUrl,
                stock = request.stock
              )
              merchandiseRepo.update(id, updatedMerch)
            case None =>
              None
          }
        case None =>
          None
      }
    }
  }

  // --- getMerchandise: Menggunakan koneksi untuk operasi baca ---
  def getMerchandise(id: Int): Future[Option[Merchandise]] = Future {
    db.withConnection { implicit connection =>
      merchandiseRepo.findById(id)
    }
  }

  // --- getAllMerchandise: Menggunakan koneksi untuk operasi baca ---
  def getAllMerchandise: Future[Seq[Merchandise]] = Future {
    db.withConnection { implicit connection =>
      merchandiseRepo.findAll()
    }
  }

  // --- deleteMerchandise: Menggunakan transaksi untuk atomicity ---
  def deleteMerchandise(id: Int): Future[Int] = Future {
    db.withTransaction { implicit connection =>
      merchandiseRepo.deleteMerch(id)
    }
  }

  // --- softDeleteMerchandise: Menggunakan transaksi untuk atomicity ---
  def softDeleteMerchandise(id: Int): Future[Boolean] = Future {
    db.withTransaction { implicit connection =>
      merchandiseRepo.softDeleteMerch(id)
    }
  }
}
