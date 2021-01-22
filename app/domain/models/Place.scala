package domain.models

import domain.DomainException.NotFoundException
import play.api.libs.json.Json
import play.api.mvc.PathBindable

import java.net.URI

case class PlaceId(value: Long) extends AnyVal {
  override def toString: String = value.toString
}

object PlaceId {
  def empty: PlaceId = PlaceId(0L)

  // This really shouldn't be in the domain model but I haven't figured out how to make
  // Play find this otherwise
  implicit def placeIdPathBinder(implicit stringBinder: PathBindable[String]): PathBindable[PlaceId] = new PathBindable[PlaceId] {
    override def bind(key: String, value: String): Either[String, PlaceId] = {
      value.toLongOption.map(PlaceId.apply) match {
        case Some(id) => Right(id)
        case None => Left(s"Invalid place id [$value]")
      }
    }

    override def unbind(key: String, placeId: PlaceId): String = {
      stringBinder.unbind(key, placeId.value.toString)
    }
  }
}

sealed trait ExternalPlaceId {
  def value: String
  def source: String
  override def toString: String = s"$source:$value"
}

object ExternalPlaceId {
  private val google = "google"

  case class GooglePlaceId(value: String) extends ExternalPlaceId {
    override def source: String = google
    override def toString: String = s"$source:$value"
  }

  def apply(
    source: String,
    id: String
  ): ExternalPlaceId = source match {
    case `google` | _ => GooglePlaceId(id) // just google for now
  }

  def apply(value: String): Option[ExternalPlaceId] = {
    value.split(":") match {
      case Array(source, id) => Some(apply(source, id))
      case _ => None
    }
  }
}

case class GeoLocation(lat: Double, lng: Double) {
  override def toString: String = s"$lat,$lng"
}

object GeoLocation {
  def parse(value: String): Option[GeoLocation] =
    Some(value).flatMap(_.split(",").map(_.toDoubleOption) match {
      case Array(Some(lat), Some(lng)) => Some(GeoLocation(lat, lng))
      case _ => None
    })

  def unapply(value: String): Option[GeoLocation] = parse(value)
}

sealed trait VisitStatus {
  def text: String
  def identifier: String
}

object VisitStatus {
  case object None extends VisitStatus {
    val text: String = ""
    val identifier: String = "none"
  }
  case object Hidden extends VisitStatus {
    val text: String = "hidden"
    val identifier: String = "hidden"
  }
  case object Saved extends VisitStatus {
    val text: String = "saved"
    val identifier: String = "saved"
  }
  case object Visited extends VisitStatus {
    val text: String = "visited"
    val identifier: String = "visited"
  }

  def apply(text: String): VisitStatus = text match {
    case Hidden.identifier => Hidden
    case Saved.identifier => Saved
    case Visited.identifier => Visited
    case None.identifier | _ => None
  }

  def unapply(status: VisitStatus): String = status.identifier
}

sealed trait OperationalStatus {
  def text: String
  def identifier: String
  def sortKey: Int
}

object OperationalStatus {
  case object Open extends OperationalStatus {
    val text: String = "probably open"
    val identifier: String = "open"
    val sortKey: Int = 0
  }
  case object Closed extends OperationalStatus {
    val text: String = "probably closed"
    val identifier: String = "closed"
    val sortKey: Int = 1
  }
  case object ClosedPermanently extends OperationalStatus {
    val text: String = "closed permanently"
    val identifier: String = "permanently_closed"
    val sortKey: Int = 2
  }
}

case class PlaceNotFoundException(id: String)
  extends Exception(s"Failed to locate place with id [$id]")
    with NotFoundException

object PlaceNotFoundException {
  def apply(placeId: PlaceId): PlaceNotFoundException =
    PlaceNotFoundException(placeId.value.toString)

  def apply(placeId: ExternalPlaceId): PlaceNotFoundException =
    PlaceNotFoundException(placeId.value)
}

case class Place(
  id: PlaceId,
  externalId: ExternalPlaceId,
  userId: UserId,
  accountId: AccountId,
  location: GeoLocation,
  name: String,
  address: String,
  icon: URI,
  operationalStatus: OperationalStatus,
  visitStatus: VisitStatus,
  commentCount: Int
)
