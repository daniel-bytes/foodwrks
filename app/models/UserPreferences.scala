package models

import domain.models._

case class UserPreferencesView(
  location: GeoLocation,
  radius: Int,
  hideClosed: Boolean,
  hidePermanentClosed: Boolean,
  lastPlace: HeaderNav
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
        Keys.lastPlace -> StringUserPreferenceValue(lastPlace.toString)
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
  }

  val default = UserPreferencesView(
    location = GeoLocation(40.761880,-73.958600),
    radius = 500,
    hideClosed = false,
    hidePermanentClosed = false,
    lastPlace = HeaderNav.NearbyPlaces
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
      lastPlace = HeaderNav(prefs.getString(Keys.lastPlace))
    )

  def unapply(prefs: UserPreferencesView): UserPreferences = prefs.toDomainModel
}
