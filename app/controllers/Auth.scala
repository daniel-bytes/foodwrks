package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Env, Identity, LoginInfo}
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import domain._
import domain.services.UsersService
import models.{User, ExternalUserId, UserNotFoundException}

import scala.concurrent.{ExecutionContext, Future}

object Auth {
  case class UserIdentity(
    user: User,
    loginInfo: LoginInfo
  ) extends Identity

  trait SessionEnv extends Env {
    type I = UserIdentity
    type A = SessionAuthenticator
  }

  class UserIdentityService @Inject()(
    usersService: UsersService
  )(implicit ec: ExecutionContext) extends IdentityService[SessionEnv#I] {
    def retrieve(loginInfo: LoginInfo): Future[Option[UserIdentity]] = {
      val userId = ExternalUserId(
        loginInfo.providerID,
        loginInfo.providerKey
      )
      usersService.getUserByExternalId(userId).flatMap {
        case Right(user) => Future.successful(Some(UserIdentity(user, loginInfo)))
        case Left(Error.NotFoundError(UserNotFoundException(_))) => Future.successful(None)
        case Left(e) => Future.failed(e.toDomainException)
      }
    }
  }
}
