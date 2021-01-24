package domain.repositories

import com.google.inject.Inject
import domain._
import domain.models._
import play.api.Logger
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._
import play.api.libs.ws._

import java.net.URI
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

case class PlacesSearchFailedException(statusCode: Int, body: String)
  extends DomainException.BackendException("Failed to find Place details from Google")

trait PlacesSearchRepository extends Repository {
  def getDetails(
    placeId: ExternalPlaceId
  ): AsyncResult[Place]

  def searchNearbyPlaces(
    location: GeoLocation,
    radius: Int,
    placeType: PlaceType
  ): AsyncResult[Seq[Place]]
}

object PlacesSearchRepository {
  private val detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json"
  private val searchUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"

  case class GooglePlacesConfig(apiKey: String)

  implicit val logger: Logger = Logger(getClass)

  class GooglePlacesSearchRepository @Inject() (
    config: GooglePlacesConfig,
    ws: WSClient
  )(implicit ec: ExecutionContext) extends PlacesSearchRepository {
    import GooglePlaces._

    def getDetails(
      placeId: ExternalPlaceId
    ): AsyncResult[Place] = {
      ws
        .url(detailsUrl)
        .withQueryStringParameters(
          "place_id" -> placeId.value,
          "key" -> config.apiKey
        )
        .withRequestTimeout(10000.millis)
        .get()
        .map { r =>
          logger.info(s"Google API [$detailsUrl?place_id=${placeId.value}] responded with [${r.status} ${r.statusText}]")
          if (r.status == 200) {
            r.json.validate[Details].fold({ err =>
              Left(Error.DeserializationError(
                err.flatMap {
                  case (k, values) => values.map(v => s"$k - ${v.message}")
                }.toSeq
              ))
            }, { details =>
              fromResult(details.result) match {
                case Some(r) => Right(r)
                case None => Left(Error.NotFoundError(PlaceNotFoundException(placeId)))
              }
            })
          } else if (r.status == 404) {
            Left(Error.NotFoundError(PlaceNotFoundException(placeId)))
          } else {
            Left(Error.BackendError(PlacesSearchFailedException(r.status, r.body)))
          }
      }
    }

    def searchNearbyPlaces(
      location: GeoLocation,
      radius: Int,
      placeType: PlaceType
    ): AsyncResult[Seq[Place]] = {
      ws
        .url(searchUrl)
        .withQueryStringParameters(
        "location" -> location.toString,
          "radius" -> toMeters(radius).toString,
          "type" -> placeType.id,
          "key" -> config.apiKey
        )
        .withRequestTimeout(10000.millis)
        .get()
        .map { r =>
          logger.info(s"Google API [$detailsUrl?location=$location&radius=${toMeters(radius).toString}&type=$placeType] responded with [${r.status} ${r.statusText}]")

          r.json.validate[SearchResults].fold({ err =>
            Left(Error.DeserializationError(
              err.flatMap {
                case (k, values) => values.map(v => s"$k - ${v.message}")
              }.toSeq
            ))
          }, { results =>
            Right(
              results
                .results
                .flatMap(fromResult)
                .sortBy(_.operationalStatus.sortKey)
              // TODO: sort by distance
            )
          })
        }
    }

    private def toMeters(feet: Int): Int =
      (feet.toDouble / 3.281).toInt

    private def fromResult(r: GooglePlaces.Result): Option[Place] = {
      (r.placeId, r.name, r.geometry) match {
        case (Some(placeId), Some(name), Some(geometry)) =>
          Some(
            Place(
              id = PlaceId.empty,
              externalId = ExternalPlaceId.GooglePlaceId(placeId),
              userId = UserId.empty,
              accountId = AccountId.empty,
              location = GeoLocation(geometry.location.lat, geometry.location.lng),
              name = name,
              address = r.vicinity.getOrElse("unknown"),
              icon = r.icon.map(i => new URI(i)).getOrElse(new URI("https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png")),
              operationalStatus = r.businessStatus match {
                case Some("OPERATIONAL") if r.openingHours.exists(_.openNow) => OperationalStatus.Open
                case Some("OPERATIONAL") => OperationalStatus.Closed
                case Some(_) => OperationalStatus.ClosedPermanently
                case None => OperationalStatus.Closed
              },
              visitStatus = VisitStatus.None,
              commentCount = 0
            )
          )
        case _ => None
        }
    }
  }

  private object GooglePlaces {
    case class Location(lat: Double, lng: Double)
    case class Geometry(location: Location)
    case class OpeningHours(openNow: Boolean)
    case class Photo(
      height: Int,
      width: Int,
      htmlAttributions: Seq[String],
      photoReference: String
    )

    case class Result(
      businessStatus: Option[String],
      geometry: Option[Geometry],
      icon: Option[String],
      name: Option[String],
      openingHours: Option[OpeningHours],
      photos: Option[Seq[Photo]],
      placeId: Option[String],
      rating: Option[Double],
      reference: Option[String],
      types: Option[Seq[String]],
      userRatingsTotal: Option[Int],
      vicinity: Option[String]
    )

    case class Details(result: Result)
    case class SearchResults(results: Seq[Result])

    implicit val config = JsonConfiguration(SnakeCase)
    implicit val locationReads = Json.reads[Location]
    implicit val geometryReads = Json.reads[Geometry]
    implicit val openingHoursReads = Json.reads[OpeningHours]
    implicit val photoReads = Json.reads[Photo]
    implicit val resultReads = Json.reads[Result]
    implicit val detailsReads: Reads[Details] = Json.reads[Details]
    implicit val searchResultsReads: Reads[SearchResults] = Json.reads[SearchResults]
  }
}
