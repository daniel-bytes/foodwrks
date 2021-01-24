package models

import domain.models.{ExternalPlaceId, PlaceId, VisitStatus}
import domain.models.{PlaceTypes => DomainPlaceTypes}
import play.api.data.Form
import play.api.data.Forms._

import java.time.format.DateTimeFormatter

trait PlaceViewLike {
  def id: String
  def externalId: String
  def name: String
  def address: String
  def icon: String
  def operationalStatus: String
  def saved: Boolean
  def visited: Boolean
  def hidden: Boolean
  def editing: Boolean
  def commentCount: Int
}

case class PlaceView(
  id: String,
  externalId: String,
  name: String,
  address: String,
  icon: String,
  operationalStatus: String,
  saved: Boolean,
  visited: Boolean,
  hidden: Boolean,
  commentCount: Int
) extends PlaceViewLike {
  def editing: Boolean = false
}

case class PlacesView(
  places: Seq[PlaceView],
  pageCursor: Option[String]
)

case class PlaceDetailsView(
  id: String,
  externalId: String,
  name: String,
  address: String,
  icon: String,
  operationalStatus: String,
  saved: Boolean,
  visited: Boolean,
  hidden: Boolean,
  commentCount: Int,
  comments: Seq[CommentView],
  users: Map[String, UserView]
) extends PlaceViewLike {
  def editing: Boolean = true

  def headerNav: Option[HeaderNav] = {
    id.toLongOption.map(PlaceId.apply) match {
      case Some(placeId) if visited => Some(HeaderNav.VisitedPlace(placeId))
      case Some(placeId) if saved => Some(HeaderNav.SavedPlace(placeId))
      case _ => None
    }
  }
}

object PlaceDetailsView {
  val formatter = DateTimeFormatter.ofPattern("E, MMM dd yyyy")
}

object PlaceForms {
  case class UpdateVisitStatus(
    externalId: ExternalPlaceId,
    visitStatus: VisitStatus
  )

  object UpdateVisitStatus {
    def apply(externalId: String, status: String): UpdateVisitStatus =
      UpdateVisitStatus(ExternalPlaceId(externalId).get, VisitStatus(status))

    def unapply(data: UpdateVisitStatus): Option[(String, String)] =
      Some(data.externalId.toString -> data.visitStatus.identifier)
  }

  val updateVisitStatusForm = Form(
    mapping(
      "external_id" -> text.verifying(id => ExternalPlaceId(id).nonEmpty),
      "visit_status" -> nonEmptyText
    )(UpdateVisitStatus.apply)(UpdateVisitStatus.unapply)
  )
}
