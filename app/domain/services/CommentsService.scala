package domain.services

import com.google.inject.Inject
import domain._
import domain.models._
import domain.repositories.CommentsRepository

import scala.concurrent.ExecutionContext

trait CommentsService extends Service {

  def listCommentsForPlace(
    placeId: PlaceId
  ): AsyncResult[Seq[Comment]]

  def createComment(
    comment: Comment
  ): AsyncResult[Unit]

  def deleteComment(
    commentId: CommentId
  ): AsyncResult[Unit]
}

object CommentsService {
  class Default @Inject()(
    commentsRepository: CommentsRepository
  )(implicit ec: ExecutionContext) extends CommentsService {
    def listCommentsForPlace(
      placeId: PlaceId
    ): AsyncResult[Seq[Comment]] = {
      commentsRepository.listCommentsForPlace(placeId)
    }

    def createComment(
      comment: Comment
    ): AsyncResult[Unit] = {
      commentsRepository.createComment(comment)
    }

    def deleteComment(
      commentId: CommentId
    ): AsyncResult[Unit] = {
      commentsRepository.deleteComment(commentId)
    }
  }
}

