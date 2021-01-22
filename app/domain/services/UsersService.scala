package domain.services

import com.google.inject.Inject
import domain._
import domain.repositories.UsersRepository
import models._

import scala.concurrent.ExecutionContext

trait UsersService extends Service {
  def getUserByExternalId(userId: ExternalUserId): AsyncResult[User]
  def listUsersForPlace(placeId: PlaceId): AsyncResult[Seq[User]]
  def saveUser(user: User): AsyncResult[User]
}

object UsersService {
  class Default @Inject()(usersRepository: UsersRepository)(implicit ec: ExecutionContext) extends UsersService {
    def getUserByExternalId(userId: ExternalUserId): AsyncResult[User] = {
      usersRepository.getUserByExternalId(userId)
    }

    def listUsersForPlace(placeId: PlaceId): AsyncResult[Seq[User]] = {
      usersRepository.listUsersForPlace(placeId)
    }

    def saveUser(user: User): AsyncResult[User] = {
      usersRepository.saveUser(user)
    }
  }
}
