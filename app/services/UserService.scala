package services

import models.User
import play.api.db.Database
import repositories.UserRepository

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject() (repo: UserRepository, db: Database)(implicit ec: ExecutionContext) {
  def createUser(user: User.UserRequest): Future[User] = Future {
    db.withConnection { implicit connection =>
      repo.create(user)
    }
  }
  def getUser(id: Int): Future[Option[User]] = Future {
    db.withConnection { implicit connection =>
      repo.findById(id)
    }
  }
  def getAllUser: Future[Seq[User]] = Future {
    db.withConnection { implicit connection =>
      repo.findAll()
    }
  }
  def updateUser(user: User): Future[Option[User]] = Future {
    db.withConnection { implicit connection =>
      repo.update(user)
    }
  }
  def deleteUser(id: Int): Future[Boolean] = Future {
    db.withConnection { implicit connection =>
      repo.softDeleteUser(id)
    }
  }
}
