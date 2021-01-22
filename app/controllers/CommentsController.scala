package controllers

import com.mohiva.play.silhouette.api.Silhouette
import domain.models._
import domain.services._
import models._
import play.api.data._
import play.api.Logger
import play.api.mvc._

import java.time.LocalDateTime
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

/**
 * Handles actions for /places/comments
 */
@Singleton
class CommentsController @Inject()(
  controllerComponents: MessagesControllerComponents,
  silhouette: Silhouette[Auth.SessionEnv],
  notesService: CommentsService
)(implicit ec: ExecutionContext) extends MessagesAbstractController(controllerComponents) {
  import CommentForms._

  implicit val logger: Logger = Logger(getClass)

  def create(placeId: PlaceId) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    silhouette.secureMessagesRequest { user =>
      createForm.bindFromRequest.fold({ result: Form[CommentCreate] =>
        // TODO: Handle errors
        logger.warn(
          "Failed to save note: " + result.errors.map(e => s"field [${e.key}]: [${e.message}]").mkString("\n")
        )
        Future.successful(
          Redirect(routes.PlacesController.get(placeId))
        )
      }, { data: CommentCreate =>
        val domainModel = Comment(
          id = CommentId.empty,
          placeId = placeId,
          userId = user.id,
          createdOn = LocalDateTime.now(),
          comment = data.comment
        )

        notesService
          .createComment(domainModel)
          .toMvcResult(_ => Redirect(routes.PlacesController.get(placeId)))
      })
    }
  }

  /**
   * DELETE /places/:id/comments/:id
   */
  def delete(placeId: PlaceId, commentId: CommentId) = silhouette.SecuredAction.async { implicit request =>
    // TODO: check note/entity ownership
    notesService.deleteComment(commentId)
      .toMvcResult(_ => Redirect(routes.PlacesController.get(placeId)))
  }
}
