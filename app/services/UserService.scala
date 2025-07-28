package services

import models.User
import repositories.UserRepository

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(repo: UserRepository)(implicit ec: ExecutionContext) {
  def createUser(user: User.UserRequest): Future[User] = repo.create(user)
  def getUser(id: Int): Future[Option[User]] = repo.findById(id)
  def getAllUser: Future[Seq[User]] = repo.findAll()
  def updateUser(user: User): Future[Option[User]] = repo.update(user)
  def deleteUser(id: Int): Future[Boolean] = repo.softDeleteUser(id)
}