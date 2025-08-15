package services

import models.{MerchType, Merchandise}
import play.api.db.Database
import repositories.{MerchTypeRepository, MerchandiseRepository}

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MerchandiseService @Inject() (
    db: Database,
    merchandiseRepo: MerchandiseRepository,
    merchTypeRepo: MerchTypeRepository
)(implicit ec: ExecutionContext) {

  def createMerchandise(request: Merchandise.MerchandiseRequest): Future[Option[Merchandise]] = Future {
    db.withTransaction { implicit connection =>
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
          Some(createdMerch)
        case None => None
      }
    }
  }

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
            case None => None
          }
        case None => None
      }
    }
  }

  def getMerchandise(id: Int): Future[Option[Merchandise.MerchandiseWithMerchType]] = Future {
    db.withConnection { implicit connection =>
      merchandiseRepo.findByIdDetail(id)
    }
  }

  def getAllMerchandise: Future[Seq[Merchandise]] = Future {
    db.withConnection { implicit connection =>
      merchandiseRepo.findAll()
    }
  }

  def deleteMerchandise(id: Int): Future[Int] = Future {
    db.withTransaction { implicit connection =>
      merchandiseRepo.deleteMerch(id)
    }
  }

  def softDeleteMerchandise(id: Int): Future[Boolean] = Future {
    db.withTransaction { implicit connection =>
      merchandiseRepo.softDeleteMerch(id)
    }
  }
}
