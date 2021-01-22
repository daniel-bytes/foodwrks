package domain.models

import domain.DomainException.NotFoundException

import java.time.LocalDateTime

case class CommentId(value: Long) extends AnyVal {
  override def toString: String = value.toString
}

object CommentId {
  def empty: CommentId = CommentId(0L)
}

case class CommentNotFoundException(placeId: PlaceId, id: CommentId)
  extends Exception(s"Failed to locate comment with id [${id.value}] for place with id [${placeId.value}]")
    with NotFoundException

case class Comment(
  id: CommentId,
  placeId: PlaceId,
  userId: UserId,
  createdOn: LocalDateTime,
  comment: String
)
