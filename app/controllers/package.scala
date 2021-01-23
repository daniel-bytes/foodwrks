import cats.data.EitherT
import com.mohiva.play.silhouette.api.{HandlerResult, Silhouette}
import domain.{Error, Service}
import domain.models.{ExternalPlaceId, User, ExternalUserId}
import models.{HeaderNav, UserPreferencesView}
import play.api.Logger
import play.api.mvc.{AnyContent, MessagesRequest, PathBindable, Result}
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

package object controllers {
  private val notFound: Result =
    NotFound("Oops, we couldn't find what you're looking for.")

  private val internalServerError: Result =
    InternalServerError("Uh oh, something bad happened. Its not you, its us.")

  implicit class DomainResultSyntax[T](val result: Service#Result[T]) extends AnyVal {
    def toMvcResult(implicit logger: Logger): Either[Result, T] = {
      result match {
        case Left(Error.NotFoundError(_)) =>
          Left(notFound)
        case Left(Error.AggregateError(errors)) =>
          Left(
            errors.collect {
              case _: Error.NotFoundError => notFound
            }.headOption.getOrElse(internalServerError)
          )
        case Left(err: Error) =>
          logger.error("A server error occurred", err.toDomainException)
          Left(internalServerError)
        case Right(value) =>
          Right(value)
      }
    }
  }

  implicit class DomainAsyncResultSyntax[T](val result: Service#AsyncResult[T]) extends AnyVal {
    def toEitherT(implicit ec: ExecutionContext): EitherT[Future, Error, T] = {
      EitherT(result)
    }

    def toMvcResultEitherT(implicit ec: ExecutionContext, logger: Logger): EitherT[Future, Result, T] = {
      EitherT(result.map(_.toMvcResult))
    }

    def toMvcResult(success: T => Result)(implicit ec: ExecutionContext, logger: Logger): Future[Result] = {
      result.map(_.toMvcResult match {
        case Left(error) => error
        case Right(value) => success(value)
      })
    }

    def toFuture(implicit ec: ExecutionContext): Future[T] = {
      result.flatMap {
        case Left(error) => Future.failed(error.toDomainException)
        case Right(value) => Future.successful(value)
      }
    }
  }

  implicit class McvAsyncResultSyntax(val result: Future[Either[Result, Result]]) extends AnyVal {
    def fold(implicit ec: ExecutionContext): Future[Result] = {
      result.map(_.fold(left => left, right => right))
    }
  }

  implicit class SilhouetteSyntax(val silhouette: Silhouette[Auth.SessionEnv]) extends AnyVal {
    def secureMessagesRequest(f: User => Future[Result])(
      implicit ec: ExecutionContext,
      request: MessagesRequest[AnyContent]
    ): Future[Result] = {
      silhouette.SecuredRequestHandler { securedRequest =>
        f(securedRequest.identity.user).map(r => HandlerResult(r, Some(securedRequest.identity)))
      }.map {
        case HandlerResult(r, Some(_)) => r
        case HandlerResult(_, None) => Redirect(routes.AuthController.login())
          .flashing("not_authenticated" -> "Authentication required")
      }
    }
  }

  implicit class UserSyntax(val user: User) extends AnyVal {
    def redirectFromPreferences(listOnly: Boolean = false): Result = UserPreferencesView(user.preferences).lastPlace match {
      case HeaderNav.NearbyPlaces => Redirect(routes.PlacesController.nearby())
      case HeaderNav.PlacesSearch => Redirect(routes.PlacesController.search())
      case HeaderNav.SavedPlaces => Redirect(routes.PlacesController.saved())
      case HeaderNav.VisitedPlaces => Redirect(routes.PlacesController.visited())
      case HeaderNav.SavedPlace(id) if listOnly => Redirect(routes.PlacesController.saved())
      case HeaderNav.SavedPlace(id) => Redirect(routes.PlacesController.get(id))
      case HeaderNav.VisitedPlace(id) if listOnly => Redirect(routes.PlacesController.visited())
      case HeaderNav.VisitedPlace(id) => Redirect(routes.PlacesController.get(id))
    }
  }
}
