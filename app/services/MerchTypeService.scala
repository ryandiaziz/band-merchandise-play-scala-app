package services

import models.MerchType
import play.api.db.Database
import repositories.MerchTypeRepository

import java.sql.Connection
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MerchTypeService @Inject() (db: Database, repo: MerchTypeRepository)(implicit ec: ExecutionContext) {

  def createMerchType(merchType: MerchType): Future[MerchType] = Future {
    db.withTransaction { implicit connection =>
      // repo.create adalah metode SINKRON, jadi kita bisa langsung memanggilnya
      repo.create(merchType)
    }
  }

  // --- Metode-metode lain juga disesuaikan untuk menggunakan Future dan db.withConnection ---

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

  // ... (Metode update, delete, dll. juga menggunakan pola Future { db.withTransaction { ... } }) ...
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