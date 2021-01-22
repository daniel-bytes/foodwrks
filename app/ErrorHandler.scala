import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent._
import javax.inject.Singleton

@Singleton
class ErrorHandler extends HttpErrorHandler {
  val logger: Logger = Logger(getClass)

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    logger.error(s"A client error occurred: $statusCode $message")

    Future.successful(
      Status(statusCode)("A client error occurred: " + message)
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    logger.error("A server error occurred", exception)

    Future.successful(
      InternalServerError("A server error occurred: " + exception.getMessage)
    )
  }
}