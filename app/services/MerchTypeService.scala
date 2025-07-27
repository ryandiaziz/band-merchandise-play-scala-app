package services

import models.MerchType
import javax.inject._
import repositories.MerchTypeRepository
import scala.concurrent.{Future, ExecutionContext}

@Singleton
class MerchTypeService @Inject()(repo: MerchTypeRepository)(implicit ec: ExecutionContext) {
  def createMerchType(merchType: MerchType): Future[MerchType] = repo.create(merchType)
  def getMerchType(id: Int): Future[Option[MerchType]] = repo.findById(id)
  def getAllMerchTypes: Future[Seq[MerchType]] = repo.findAll()
  def updateMerchType(merchType: MerchType): Future[Option[MerchType]] = repo.update(merchType)
  def deleteMerchType(id: Int): Future[Boolean] = repo.softDeleteMerchType(id)
}