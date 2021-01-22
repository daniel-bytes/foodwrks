package domain.repositories

import com.google.inject.Inject
import domain.models._
import play.api.db.Database

import java.sql.{Connection, ResultSet}
import scala.concurrent.Future

trait CommentsRepository extends Repository {
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

object CommentsRepository {
  class SqlDatabase @Inject()(db: Database, databaseExecutionContext: DatabaseExecutionContext) extends CommentsRepository {
    def listCommentsForPlace(
      placeId: PlaceId
    ): AsyncResult[Seq[Comment]] = {
      Future {
        db.withConnection { conn =>
          Right(getPlaceCommentsByPlaceIdQuery(conn, placeId))
        }
      }(databaseExecutionContext)
    }

    def createComment(
      comment: Comment
    ): AsyncResult[Unit] = {
      Future {
        db.withConnection { conn =>
          Right(
            insertPlaceCommentCommand(conn, comment)
          )
        }
      }(databaseExecutionContext)
    }

    def deleteComment(
      commentId: CommentId
    ): AsyncResult[Unit] = {
      Future {
        db.withConnection { conn =>
          Right(
            deletePlaceCommentCommand(conn, commentId)
          )
        }
      }(databaseExecutionContext)
    }

    private def getPlaceCommentsByPlaceIdQuery(conn: Connection, placeId: PlaceId): Seq[Comment] = {
      val query = conn.prepareStatement("SELECT * FROM get_place_comments_by_place_id(?)")
      query.setLong(1, placeId.value)
      query.executeQuery().map(commentFromResultSet).toSeq
    }

    private def insertPlaceCommentCommand(conn: Connection, comment: Comment): Unit = {
      val command = conn.prepareCall("CALL insert_place_comment(?, ?, ?)")
      command.setLong(1, comment.placeId.value)
      command.setLong(2, comment.userId.value)
      command.setString(3, comment.comment)
      command.execute()
    }

    private def deletePlaceCommentCommand(conn: Connection, commentId: CommentId): Unit = {
      val command = conn.prepareCall("CALL delete_place_comment(?)")
      command.setLong(1, commentId.value.toLong)
      command.execute()
    }

    private def commentFromResultSet(rs: ResultSet): Comment = {
      Comment(
        id = CommentId(rs.getLong(1)),
        placeId = PlaceId(rs.getLong(2)),
        userId = UserId(rs.getLong(3)),
        createdOn = rs.getTimestamp(4).toLocalDateTime,
        comment = rs.getString(5)
      )
    }
  }
}