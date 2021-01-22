package models

import domain.models._
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}

import java.time.LocalDateTime

case class CommentView(
  id: String,
  placeId: String,
  userId: String,
  createdOn: LocalDateTime,
  comment: String
)

object CommentView {
  def apply(comment: Comment): CommentView =
    CommentView(
      id = comment.id.value.toString,
      placeId = comment.placeId.toString,
      userId = comment.userId.toString,
      createdOn = comment.createdOn,
      comment = comment.comment
    )
}

object CommentForms {
  case class CommentCreate(comment: String)

  val createForm = Form(
    mapping(
      "comment" -> nonEmptyText
    )(CommentCreate.apply)(CommentCreate.unapply)
  )
}