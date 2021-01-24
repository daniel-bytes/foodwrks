package domain.services

import com.google.inject.Inject
import com.softwaremill.quicklens._
import domain._
import domain.models._
import domain.repositories.{PlacesRepository, PlacesSearchRepository}

import scala.concurrent.{ExecutionContext, Future}

trait PlacesService extends Service {

  def searchNearbyPlaces(
    userId: UserId,
    location: GeoLocation,
    radius: Int,
    placeType: PlaceType,
    pageCursor: Option[PageCursor]
  ): AsyncResult[CursorPagedSeq[Place]]

  def searchPlaces(
    userId: UserId,
    location: GeoLocation,
    radius: Int,
    query: String
  ): AsyncResult[CursorPagedSeq[Place]]

  def listPlacesForUser(
    userId: UserId,
    visitStatus: Option[VisitStatus] = None
  ): AsyncResult[CursorPagedSeq[Place]]

  def getPlace(
    placeId: PlaceId
  ): AsyncResult[Place]

  def getPlaceByExternalId(
    externalId: ExternalPlaceId,
    userId: UserId
  ): AsyncResult[Place]

  def savePlace(
    place: Place
  ): AsyncResult[Place]

  def deletePlace(
    placeId: PlaceId
  ): AsyncResult[Unit]
}

object PlacesService {
  class Default @Inject()(
    placesRepository: PlacesRepository,
    searchRepository: PlacesSearchRepository
  )(implicit ec: ExecutionContext) extends PlacesService {
    def searchNearbyPlaces(
      userId: UserId,
      location: GeoLocation,
      radius: Int,
      placeType: PlaceType,
      pageCursor: Option[PageCursor]
    ): AsyncResult[CursorPagedSeq[Place]] = {
      (for {
        nearby <- searchRepository.searchNearbyPlaces(location, radius, placeType, pageCursor).toEitherT

        saved <- placesRepository.listPlacesForUser(userId).toEitherT

        results = join(nearby, saved)
      } yield results).value
    }

    def searchPlaces(
      userId: UserId,
      location: GeoLocation,
      radius: Int,
      query: String
    ): AsyncResult[CursorPagedSeq[Place]] = {
      (for {
        nearby <- searchRepository.searchPlaces(location, radius, query).toEitherT

        saved <- placesRepository.listPlacesForUser(userId).toEitherT

        results = join(nearby, saved)
      } yield results).value
    }

    def listPlacesForUser(
      userId: UserId,
      visitStatus: Option[VisitStatus] = None
    ): AsyncResult[CursorPagedSeq[Place]] = {
      placesRepository.listPlacesForUser(userId, visitStatus)
    }

    def getPlace(
      placeId: PlaceId
    ): AsyncResult[Place] = {
      placesRepository.getPlace(placeId)
    }

    def getPlaceByExternalId(
      externalId: ExternalPlaceId,
      userId: UserId
    ): AsyncResult[Place] = {
      (for {
        googlePlace <- searchRepository.getDetails(externalId).toEitherT

        place <- placesRepository.getPlaceByExternalId(externalId).map {
          case Right(savedPlace) =>
            Right(merge(savedPlace, googlePlace))
          case Left(Error.NotFoundError(PlaceNotFoundException(_))) =>
            Right(googlePlace.modify(_.userId).setTo(userId))
          case Left(e) =>
            Left(e)
        }.toEitherT
      } yield place).value
    }

    def savePlace(
      place: Place
    ): AsyncResult[Place] = {
      (for {
        saved <- placesRepository.savePlace(place).toEitherT

        result <- if (saved.id == PlaceId.empty) {
          getPlaceByExternalId(place.externalId, place.userId).toEitherT
        } else {
          Future.successful(Right(saved)).toEitherT
        }
      } yield result).value
    }

    def deletePlace(
      placeId: PlaceId
    ): AsyncResult[Unit] = {
      placesRepository.deletePlace(placeId)
    }
  }

  def join(
    externalPlaces: CursorPagedSeq[Place],
    savedPlaces: CursorPagedSeq[Place]
  ): CursorPagedSeq[Place] = {
    val data = externalPlaces.data.map { externalPlace =>
      savedPlaces
        .data
        .find(_.externalId == externalPlace.externalId)
        .map(merge(_, externalPlace))
        .getOrElse(externalPlace)
    }.filter(_.visitStatus != VisitStatus.Hidden)

    CursorPagedSeq(data, externalPlaces.cursor)
  }

  def merge(
    savedPlace: Place,
    externalPlace: Place
  ): Place = {
    externalPlace
      .modify(_.id)
      .setTo(savedPlace.id)
      .modify(_.visitStatus)
      .setTo(savedPlace.visitStatus)
      .modify(_.userId)
      .setTo(savedPlace.userId)
      .modify(_.accountId)
      .setTo(savedPlace.accountId)
      .modify(_.commentCount)
      .setTo(savedPlace.commentCount)
  }
}
