package models

import domain.models._

case class UserPreferencesView(
  location: GeoLocation,
  radius: Int,
  hideClosed: Boolean,
  hidePermanentClosed: Boolean,
  lastPlace: HeaderNav,
  placeType: PlaceType
) {
  def toDomainModel: UserPreferences = {
    import UserPreferencesView.Keys

    UserPreferences(
      Map(
        Keys.locationLat -> NumberUserPreferenceValue(location.lat),
        Keys.locationLng -> NumberUserPreferenceValue(location.lng),
        Keys.radius -> NumberUserPreferenceValue(radius),
        Keys.hideClosed -> BooleanUserPreferenceValue(hideClosed),
        Keys.hidePermanentClosed -> BooleanUserPreferenceValue(hidePermanentClosed),
        Keys.lastPlace -> StringUserPreferenceValue(lastPlace.toString),
        Keys.placeType -> StringUserPreferenceValue(placeType.id)
      )
    )
  }
}

object UserPreferencesView {
  object Keys {
    final val locationLat = "site.location.lat"
    final val locationLng = "site.location.lng"
    final val radius = "site.radius"
    final val hideClosed = "site.hide_closed"
    final val hidePermanentClosed = "site.hide_permanent_closed"
    final val lastPlace = "site.last_place"
    final val placeType = "site.place_type"
  }

  val default = UserPreferencesView(
    location = GeoLocation(40.761880,-73.958600),
    radius = 500,
    hideClosed = false,
    hidePermanentClosed = false,
    lastPlace = HeaderNav.NearbyPlaces,
    placeType = PlaceTypes.Restaurant
  )

  def apply(prefs: UserPreferences): UserPreferencesView =
    UserPreferencesView(
      location = GeoLocation(
        lat = prefs.getDouble(Keys.locationLat),
        lng = prefs.getDouble(Keys.locationLng)
      ),
      radius = prefs.getInt(Keys.radius),
      hideClosed = prefs.getBoolean(Keys.hideClosed),
      hidePermanentClosed = prefs.getBoolean(Keys.hidePermanentClosed),
      lastPlace = HeaderNav(prefs.getString(Keys.lastPlace)),
      placeType = PlaceTypes.byId(prefs.getString(Keys.placeType)).getOrElse(PlaceTypes.Restaurant)
    )

  def unapply(prefs: UserPreferencesView): UserPreferences = prefs.toDomainModel
}
