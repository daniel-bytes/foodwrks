package models

import domain.models.{ExternalPlaceId, PlaceId}

sealed trait HeaderNav

// Be careful changing these names, as they are used in serialized user preferences in the database
object HeaderNav {
  case object NearbyPlaces extends HeaderNav

  case object PlacesSearch extends HeaderNav

  case object SavedPlaces extends HeaderNav

  case object VisitedPlaces extends HeaderNav

  case class SavedPlace(placeId: PlaceId) extends HeaderNav {
    override def toString: String = s"SavedPlace:${placeId.value}"
  }

  case class VisitedPlace(placeId: PlaceId) extends HeaderNav {
    override def toString: String = s"VisitedPlace:${placeId.value}"
  }

  def parse(str: String): HeaderNav = {
    str.split(":") match {
      case Array("NearbyPlaces") => NearbyPlaces
      case Array("PlacesSearch") => PlacesSearch
      case Array("SavedPlaces") => SavedPlaces
      case Array("VisitedPlaces") => VisitedPlaces
      case Array("SavedPlace", maybeId) =>
        maybeId.toLongOption.map(id => SavedPlace(PlaceId(id))).getOrElse(SavedPlaces)
      case Array("VisitedPlace", maybeId) =>
        maybeId.toLongOption.map(id => VisitedPlace(PlaceId(id))).getOrElse(VisitedPlaces)
      case _ => NearbyPlaces
    }
  }

  def apply(str: String): HeaderNav = parse(str)
}

case class Site[Model](
  headerNav: HeaderNav,
  preferences: UserPreferencesView,
  model: Model
)

object Site {
  def apply[View](
    preferences: UserPreferencesView,
    model: View
  ): Site[View] = {
    Site(
      headerNav = preferences.lastPlace,
      preferences = preferences,
      model = model
    )
  }
}