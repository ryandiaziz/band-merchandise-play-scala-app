package services

import models.MerchType
import play.api.db.Database
import repositories.MerchTypeRepository

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MerchTypeService @Inject() (db: Database, repo: MerchTypeRepository)(implicit ec: ExecutionContext) {

  def createMerchType(merchType: MerchType): Future[MerchType] = Future {
    db.withTransaction { implicit connection =>
      repo.create(merchType)
    }
  }

  def getMerchType(id: Int): Future[Option[MerchType]] = Future {
    db.withConnection { implicit connection =>
      repo.findById(id)
    }
  }

  def getAllMerchTypes: Future[Seq[MerchType]] = Future {
    db.withConnection { implicit connection =>
      repo.findAll()
    }
  }

  def updateMerchType(merchType: MerchType): Future[Option[MerchType]] = Future {
    db.withTransaction { implicit connection =>
      repo.update(merchType)
    }
  }

  def deleteMerchType(id: Int): Future[Boolean] = Future {
    db.withTransaction { implicit connection =>
      repo.softDeleteMerchType(id)
    }
  }
}
