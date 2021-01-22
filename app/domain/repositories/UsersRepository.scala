package domain.repositories

import com.google.inject.Inject
import domain._
import domain.models._
import play.api.db.Database

import java.sql.{Connection, ResultSet, Types}
import scala.concurrent.Future

trait UsersRepository extends Repository {
  def getUserByExternalId(
    userId: ExternalUserId
  ): AsyncResult[User]

  def listUsersForPlace(
    placeId: PlaceId
  ): AsyncResult[Seq[User]]

  def saveUser(
    user: User
  ): AsyncResult[User]
}

object UsersRepository {

  class SqlDatabase @Inject()(
    db: Database,
    databaseExecutionContext: DatabaseExecutionContext
  ) extends UsersRepository {
    def getUserByExternalId(userId: ExternalUserId): AsyncResult[User] = {
      Future {
        db.withConnection { conn =>
          getUserByExternalIdQuery(conn, userId) match {
            case Some(user) => Right(user)
            case None => Left(Error.NotFoundError(UserNotFoundException(userId)))
          }
        }
      }(databaseExecutionContext)
    }

    def listUsersForPlace(placeId: PlaceId): AsyncResult[Seq[User]] = {
      Future {
        db.withConnection { conn =>
          val users = getUsersByPlaceIdQuery(conn, placeId)
          Right(users)
        }
      }(databaseExecutionContext)
    }

    def saveUser(user: User): AsyncResult[User] = {
      Future {
        db.withConnection { conn =>
          upsertUserCommand(conn, user)
          Right(user)
        }
      }(databaseExecutionContext)
    }

    private def getUserByExternalIdQuery(conn: Connection, userId: ExternalUserId): Option[User] = {
      val query = conn.prepareStatement("SELECT * FROM get_user_by_external_id(?, ?)")
      query.setString(1, userId.source)
      query.setString(2, userId.value)
      query.executeQuery().getOpt(userFromResultSet)
    }

    private def getUsersByPlaceIdQuery(conn: Connection, placeId: PlaceId): Seq[User] = {
      val query = conn.prepareStatement("SELECT * FROM list_users_by_place_id(?)")
      query.setLong(1, placeId.value)
      query.executeQuery().map(userFromResultSet).toSeq
    }

    private def upsertUserCommand(conn: Connection, user: User): Unit = {
      val command = conn.prepareCall("CALL upsert_user(?, ?, ?, ?, ?, ?::jsonb)")
      command.setString(1, user.externalId.source)
      command.setString(2, user.externalId.value)
      if (user.accountId.isEmpty) command.setNull(3, Types.BIGINT)
      else command.setLong(3, user.accountId.value)
      command.setString(4, user.name)
      command.setString(5, user.email.value)
      command.setObject(6, user.preferences.toJsonString)
      command.execute()
    }

    private def userFromResultSet(rs: ResultSet): User = {
      User(
        id = UserId(rs.getLong(1)),
        externalId = ExternalUserId(
          rs.getString(2),
          rs.getString(3)
        ),
        accountId = AccountId(rs.getLong(4)),
        name = rs.getString(5),
        email = UserEmail(rs.getString(6)),
        preferences = UserPreferences(rs.getString(7))
      )
    }
  }
}
