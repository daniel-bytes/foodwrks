package domain.repositories

import com.google.inject.Inject
import domain._
import domain.models._
import play.api.db.Database

import java.net.URI
import java.sql.{Connection, ResultSet, Types}
import scala.concurrent.Future

trait PlacesRepository extends Repository {
  def listPlacesForUser(
    userId: UserId,
    visitStatus: Option[VisitStatus] = None
  ): AsyncResult[CursorPagedSeq[Place]]

  def getPlace(
    placeId: PlaceId
  ): AsyncResult[Place]

  def getPlaceByExternalId(
    externalId: ExternalPlaceId
  ): AsyncResult[Place]

  def savePlace(
    place: Place
  ): AsyncResult[Place]

  def deletePlace(
    placeId: PlaceId
  ): AsyncResult[Unit]
}

object PlacesRepository {
  class SqlDatabase @Inject()(db: Database, databaseExecutionContext: DatabaseExecutionContext) extends PlacesRepository {
    def listPlacesForUser(
      userId: UserId,
      visitStatus: Option[VisitStatus] = None
    ): AsyncResult[CursorPagedSeq[Place]] = {
      Future {
        db.withConnection { conn =>
          Right(
            CursorPagedSeq(listPlacesByUserIdQuery(conn, userId, visitStatus), cursor = None)
          )
        }
      }(databaseExecutionContext)
    }

    def getPlace(
      placeId: PlaceId
    ): AsyncResult[Place] = {
      Future {
        db.withConnection { conn =>
          getPlaceByIdQuery(conn, placeId) match {
            case Some(place) =>
              Right(place)
            case None =>
              Left(Error.NotFoundError(PlaceNotFoundException(placeId)))
          }
        }
      }(databaseExecutionContext)
    }

    def getPlaceByExternalId(
      externalId: ExternalPlaceId
    ): AsyncResult[Place] = {
      Future {
        db.withConnection { conn =>
          getPlaceByExternalIdQuery(conn, externalId) match {
            case Some(place) =>
              Right(place)
            case None =>
              Left(Error.NotFoundError(PlaceNotFoundException(externalId)))
          }
        }
      }(databaseExecutionContext)
    }

    def savePlace(
      place: Place
    ): AsyncResult[Place] = {
      Future {
        db.withConnection { conn =>
          upsertPlaceCommand(conn, place)
        }
        Right(place)
      }(databaseExecutionContext)
    }

    def deletePlace(
      placeId: PlaceId
    ): AsyncResult[Unit] = {
      Future {
        db.withConnection { conn =>
          Right(
            deletePlaceCommand(conn, placeId)
          )
        }
      }(databaseExecutionContext)
    }

    private def listPlacesByUserIdQuery(conn: Connection, userId: UserId, visitStatus: Option[VisitStatus] = None): Seq[Place] = {
      val query = conn.prepareStatement("SELECT * FROM list_places_by_user_id(?, ?)")
      query.setLong(1, userId.value)
      visitStatus.map(_.identifier) match {
        case Some(status) => query.setString(2, status)
        case None => query.setNull(2, Types.VARCHAR)
      }
      query.executeQuery().map(placeFromResultSet).toSeq
    }

    private def getPlaceByIdQuery(conn: Connection, placeId: PlaceId): Option[Place] = {
      val query = conn.prepareStatement("SELECT * FROM get_place_by_id(?)")
      query.setLong(1, placeId.value)
      query.executeQuery().getOpt(placeFromResultSet)
    }

    private def getPlaceByExternalIdQuery(conn: Connection, externalId: ExternalPlaceId): Option[Place] = {
      val query = conn.prepareStatement("SELECT * FROM get_place_by_external_id(?, ?)")
      query.setString(1, externalId.source)
      query.setString(2, externalId.value)
      query.executeQuery().getOpt(placeFromResultSet)
    }

    private def upsertPlaceCommand(conn: Connection, place: Place): Unit = {
      val command = conn.prepareCall("CALL upsert_place(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
      command.setString(1, place.externalId.source)
      command.setString(2, place.externalId.value)
      command.setLong(3, place.userId.value)
      command.setLong(4, place.accountId.value)
      command.setString(5, place.name)
      command.setString(6, place.address)
      command.setString(7, place.icon.toString)
      command.setString(8, place.visitStatus.identifier)
      command.setDouble(9, place.location.lat)
      command.setDouble(10, place.location.lng)
      command.execute()
    }

    private def deletePlaceCommand(conn: Connection, placeId: PlaceId): Unit = {
      val command = conn.prepareCall("CALL delete_place(?)")
      command.setLong(1, placeId.value)
      command.execute()
    }

    private def placeFromResultSet(rs: ResultSet): Place = {
      Place(
        id = PlaceId(rs.getLong(1)),
        externalId = ExternalPlaceId(
          rs.getString(2),
          rs.getString(3)
        ),
        userId = UserId(rs.getLong(4)),
        accountId = AccountId(rs.getLong(5)),
        name = rs.getString(6),
        address = rs.getString(7),
        icon = new URI(rs.getString(8)),
        operationalStatus = OperationalStatus.Open,
        visitStatus = rs.getString(9) match {
          case "visited" => VisitStatus.Visited
          case "hidden" => VisitStatus.Hidden
          case "saved" => VisitStatus.Saved
          case _ => VisitStatus.None
        },
        location = GeoLocation(
          lat = rs.getDouble(10),
          lng = rs.getDouble(11)
        ),
        commentCount = rs.getInt(12)
      )
    }
  }
}