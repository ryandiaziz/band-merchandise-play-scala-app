package services

import models.{MerchType, Merchandise}
import repositories.{MerchTypeRepository, MerchandiseRepository}

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MerchandiseService @Inject() (
    merchandiseRepo: MerchandiseRepository,
    merchTypeRepo: MerchTypeRepository
)(implicit ec: ExecutionContext) {

  def createMerchandise(request: Merchandise.MerchandiseRequest): Future[Either[String, Merchandise]] = {
    merchTypeRepo.findById(request.merchTypeId).flatMap {
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
        merchandiseRepo.create(newMerch).map(Right(_))
      case None =>
        Future.successful(Left(s"MerchType ID ${request.merchTypeId} not found."))
    }
  }

  def updateMerchType(id: Int, request: Merchandise.MerchandiseRequest): Future[Option[Merchandise]] =
    merchandiseRepo.update(id, request)
  
  def getMerchandise(id: Int): Future[Option[Merchandise]] = merchandiseRepo.findById(id)

  def getAllMerchandise: Future[Seq[Merchandise]] = merchandiseRepo.findAll()

  def deleteMerchandise(id: Int): Future[Int] = merchandiseRepo.deleteMerch(id)

  def softDeleteMerchandise(id: Int): Future[Boolean] = merchandiseRepo.softDeleteMerch(id)
}
