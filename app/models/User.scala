package models

import domain.models._

case class UserView(
  id: Option[String],
  name: String
)

object UserView {
  def apply(user: User): UserView =
    UserView(
      id = Some(user.id.toString),
      name = user.name
    )

  val unknown: UserView = UserView(None, "unknown")
}
