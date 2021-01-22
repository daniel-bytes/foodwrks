package domain.models

import domain.DomainException.NotFoundException
import play.api.mvc.PathBindable

case class AccountId(value: Long) extends AnyVal {
  override def toString: String = value.toString
  def isEmpty = value == 0L
}

object AccountId {
  def empty: AccountId = AccountId(0L)
}

case class UserId(value: Long) extends AnyVal {
  override def toString: String = value.toString
  def isEmpty = value == 0L
}

object UserId {
  def empty: UserId = UserId(0L)

  // This really shouldn't be in the domain model but I haven't figured out how to make
  // Play find this otherwise
  implicit def userIdPathBinder(implicit stringBinder: PathBindable[String]): PathBindable[UserId] = new PathBindable[UserId] {
    override def bind(key: String, value: String): Either[String, UserId] = {
      value.toLongOption.map(UserId.apply) match {
        case Some(id) => Right(id)
        case None => Left(s"Invalid user id [$value]")
      }
    }

    override def unbind(key: String, userId: UserId): String = {
      stringBinder.unbind(key, userId.value.toString)
    }
  }
}

sealed trait ExternalUserId {
  def value: String
  def source: String
  override def toString: String = s"$source:$value"
}

object ExternalUserId {
  private val google = "google"

  case class GoogleUserId(value: String) extends ExternalUserId {
    override def source: String = google
    override def toString: String = s"$source:$value"
  }

  def apply(
    source: String,
    id: String
  ): ExternalUserId = source match {
    case `google` | _ => GoogleUserId(id) // just google for now
  }

  def apply(value: String): Option[ExternalUserId] = {
    value.split(":") match {
      case Array(source, id) => Some(apply(source, id))
      case _ => None
    }
  }
}

case class UserEmail(value: String) extends AnyVal

object UserEmail {
  val unknown = UserEmail("unknown@unknown.com")
}

case class UserNotFoundException(id: String)
  extends Exception(s"Failed to locate user with id [$id]")
    with NotFoundException

object UserNotFoundException {
  def apply(id: UserId): UserNotFoundException = UserNotFoundException(id.value.toString)
  def apply(id: ExternalUserId): UserNotFoundException = UserNotFoundException(id.toString)
}

case class User(
  id: UserId,
  externalId: ExternalUserId,
  accountId: AccountId,
  name: String,
  email: UserEmail,
  preferences: UserPreferences
)
