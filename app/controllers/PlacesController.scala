package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.softwaremill.quicklens._
import domain.models._
import domain.services._
import models.UserPreferencesView.unapply
import models._
import play.api.mvc._
import play.api.Logger
import play.api.data.Form

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

/**
 * Handles actions for /places
 */
@Singleton
class PlacesController @Inject()(
  controllerComponents: MessagesControllerComponents,
  silhouette: Silhouette[Auth.SessionEnv],
  notesService: CommentsService,
  placesService: PlacesService,
  usersService: UsersService
)(implicit ec: ExecutionContext) extends MessagesAbstractController(controllerComponents) {
  implicit val logger: Logger = Logger(getClass)

  /**
   * GET /places/:id
   */
  def get(placeId: PlaceId) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    silhouette.secureMessagesRequest { user =>
      logger.info(s"[${user.id}] GET /places/${placeId}")
      (for {
        view <- getPlaceDetailsView(placeId).toMvcResultEitherT

        prefs <- updateUserHeaderNavPreferences(user, view.headerNav).toMvcResultEitherT

        model = Site(prefs, view)

        result = Ok(views.html.edit_place(model, CommentForms.createForm))
      } yield result).value.fold
    }
  }

  /**
   * GET /places/saved
   */
  def saved() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    silhouette.secureMessagesRequest { user =>
      logger.info(s"[${user.id}] GET /places/saved")
      (for {
        places <- placesService.listPlacesForUser(user.id, Some(VisitStatus.Saved)).toMvcResultEitherT

        view = mapPlacesView(places)

        prefs <- updateUserHeaderNavPreferences(user, Some(HeaderNav.SavedPlaces)).toMvcResultEitherT

        model = Site(prefs, view)

        result = Ok(views.html.saved_places(model, visitStatus = VisitStatus.Saved))
      } yield result).value.fold
    }
  }

  /**
   * GET /places/visited
   */
  def visited() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    silhouette.secureMessagesRequest { user =>
      logger.info(s"[${user.id}] GET /places/visited")
      (for {
        places <- placesService.listPlacesForUser(user.id, Some(VisitStatus.Visited)).toMvcResultEitherT

        view = mapPlacesView(places)

        prefs <- updateUserHeaderNavPreferences(user, Some(HeaderNav.VisitedPlaces)).toMvcResultEitherT

        model = Site(prefs, view)

        result = Ok(views.html.saved_places(model, visitStatus = VisitStatus.Visited))
      } yield result).value.fold
    }
  }

  /**
   * GET /places/hidden
   */
  def hidden() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    silhouette.secureMessagesRequest { user =>
      logger.info(s"[${user.id}] GET /places/hidden")
      (for {
        places <- placesService.listPlacesForUser(user.id, Some(VisitStatus.Hidden)).toMvcResultEitherT

        model = Site(UserPreferencesView(user.preferences), mapPlacesView(places))

        result = Ok(views.html.saved_places(model, visitStatus = VisitStatus.Hidden))
      } yield result).value.fold
    }
  }

  /**
   * GET /places/nearby
   */
  def nearby() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    silhouette.secureMessagesRequest { user =>
      logger.info(s"[${user.id}] GET /places/nearby")

      val userPrefsView = UserPreferencesView(user.preferences)
      val searchLocation = request.getQueryString("location").flatMap(GeoLocation.parse).getOrElse(userPrefsView.location)
      val searchRadius = request.getQueryString("radius").flatMap(_.toIntOption).getOrElse(userPrefsView.radius)
      val placeType = request.getQueryString("place_type").flatMap(PlaceTypes.byId).getOrElse(userPrefsView.placeType)
      val pageCursor = request.getQueryString("page_cursor").map(PageCursor)

      val headerNav = HeaderNav.NearbyPlaces
      val updatedPrefs = userPrefsView
        .modify(_.location)
        .setTo(searchLocation)
        .modify(_.radius)
        .setTo(searchRadius)
        .modify(_.lastPlace)
        .setTo(headerNav)
        .modify(_.placeType)
        .setTo(placeType)

      val updatedUser = user
        .modify(_.preferences)
        .setTo(updatedPrefs.toDomainModel)

      (for {
        places <- placesService.searchNearbyPlaces(
          user.id,
          searchLocation,
          searchRadius,
          placeType,
          pageCursor
        ).toMvcResultEitherT

        view = mapPlacesView(places)

        _ <- if (user != updatedUser) {
          usersService.saveUser(updatedUser).toMvcResultEitherT
        } else {
          Future.successful(Right(updatedUser)).toMvcResultEitherT
        }

        model = Site(updatedPrefs, view)

        placeTypes = PlaceTypes.all.values.toList.sortBy(_.text)

        result = Ok(views.html.nearby_places(model, placeTypes))
      } yield result).value.fold
    }
  }

  def search() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    silhouette.secureMessagesRequest { user =>
      logger.info(s"[${user.id}] GET /places/search")

      val headerNav = HeaderNav.PlacesSearch
      val userPrefsView = UserPreferencesView(user.preferences)
      val maybeQuery = request.getQueryString("query").map(_.trim).filter(_.nonEmpty)
      val pageCursor = request.getQueryString("page_cursor").map(PageCursor)

      maybeQuery match {
        case None =>
          Future.successful(
            Ok(
              views.html.search_places(
                Site(userPrefsView, PlacesView(Seq.empty, pageCursor = None)),
                maybeQuery
              )
            )
          )
        case Some(query) =>
          logger.info(s"search query [$query]")
          val searchLocation = request.getQueryString("location").flatMap(GeoLocation.parse).getOrElse(userPrefsView.location)
          val searchRadius = request.getQueryString("radius").flatMap(_.toIntOption).getOrElse(userPrefsView.radius)

          val updatedPrefs = userPrefsView
            .modify(_.location)
            .setTo(searchLocation)
            .modify(_.radius)
            .setTo(searchRadius)
            .modify(_.lastPlace)
            .setTo(headerNav)

          val updatedUser = user
            .modify(_.preferences)
            .setTo(updatedPrefs.toDomainModel)

          (for {
            places <- placesService.searchPlaces(
              user.id,
              searchLocation,
              searchRadius,
              query,
              pageCursor
            ).toMvcResultEitherT

            view = mapPlacesView(places)

            _ <- if (user != updatedUser) {
              usersService.saveUser(updatedUser).toMvcResultEitherT
            } else {
              Future.successful(Right(updatedUser)).toMvcResultEitherT
            }

            model = Site(updatedPrefs, view)

            result = Ok(views.html.search_places(model, maybeQuery))
          } yield result).value.fold
      }
    }
  }

  /**
   * POST /places/:id
   */
  def save(placeId: PlaceId) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    import PlaceForms._

    silhouette.secureMessagesRequest { user =>
      logger.info(s"[${user.id}] POST /places/${placeId}")

      updateVisitStatusForm.bindFromRequest.fold(
        { result: Form[UpdateVisitStatus] =>
          // TODO: Handle errors
          logger.warn(
            "Failed to save place: " + result.errors.map(e => s"field [${e.key}]: [${e.message}]").mkString("\n")
          )
          Future.successful(
            Redirect(routes.PlacesController.get(placeId))
          )
        }, { data: UpdateVisitStatus =>
          (for {
            externalPlace <- placesService.getPlaceByExternalId(data.externalId, user.id).toMvcResultEitherT

            place <- if (placeId == PlaceId.empty) {
              Future.successful(Right(externalPlace)).toMvcResultEitherT
            } else {
              placesService.getPlace(placeId).toMvcResultEitherT.map(PlacesService.merge(_, externalPlace))
            }

            placeToSave = place
              .modify(_.visitStatus)
              .setTo(data.visitStatus)
              .modify(_.userId)
              .setTo(user.id)
              .modify(_.accountId)
              .setTo(user.accountId)

            savedPlace <- placesService.savePlace(placeToSave).toMvcResultEitherT

            result = data.visitStatus match {
              case VisitStatus.Hidden => user.redirectFromPreferences(listOnly = true)
              case _ => Redirect(routes.PlacesController.get(savedPlace.id))
            }

          } yield result).value.fold
        }
      )
    }
  }

  /**
   * DELETE /places/:id
   */
  def delete(placeId: PlaceId) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    silhouette.secureMessagesRequest { user =>
      logger.info(s"[${user.id}] DELETE /places/${placeId}")

      val lastPlace = UserPreferencesView(user.preferences).lastPlace

      placesService.deletePlace(placeId)
        .toMvcResult(_ => lastPlace match {
          case HeaderNav.SavedPlace(_) => Redirect(routes.PlacesController.saved())
          case HeaderNav.VisitedPlace(_) => Redirect(routes.PlacesController.visited())
          case _ => user.redirectFromPreferences(listOnly = true)
        })
    }
  }

  def mapPlacesView(places: CursorPagedSeq[Place]): PlacesView = {
    PlacesView(
      places = places.data.map(place => PlaceView(
        place.id.toString,
        place.externalId.toString,
        place.name,
        place.address,
        place.icon.toString,
        place.operationalStatus.text,
        saved = place.visitStatus == VisitStatus.Saved,
        visited = place.visitStatus == VisitStatus.Visited,
        hidden = place.visitStatus == VisitStatus.Hidden,
        commentCount = place.commentCount
      )),
      pageCursor = places.cursor.map(_.value)
    )
  }

  def getPlaceDetailsView(placeId: PlaceId): domain.Service#AsyncResult[PlaceDetailsView] = {
    (for {
      place <- placesService.getPlace(placeId).toEitherT
      comments <- notesService.listCommentsForPlace(placeId).toEitherT
      users <- usersService.listUsersForPlace(placeId).toEitherT
      result = PlaceDetailsView(
        place.id.toString,
        place.externalId.toString,
        place.name,
        place.address,
        place.icon.toString,
        place.operationalStatus.text,
        saved = place.visitStatus == VisitStatus.Saved,
        visited = place.visitStatus == VisitStatus.Visited,
        hidden = place.visitStatus == VisitStatus.Hidden,
        commentCount = place.commentCount,
        comments = comments.map(CommentView(_)),
        users = users.map(u => u.id.toString -> UserView(u)).toMap
      )
    } yield result).value
  }


  def updateUserHeaderNavPreferences(user: User, maybeHeaderNav: Option[HeaderNav]): domain.Service#AsyncResult[UserPreferencesView] = {
    val userPrefs = UserPreferencesView(user.preferences)

    val updatedPrefs = maybeHeaderNav.map { headerNav =>
      userPrefs
        .modify(_.lastPlace)
        .setTo(headerNav)
    } getOrElse { userPrefs }

    val updatedUser = user
      .modify(_.preferences)
      .setTo(unapply(updatedPrefs))

    if (user != updatedUser)
      usersService.saveUser(updatedUser).map(_ => Right(updatedPrefs))
    else
      Future.successful(Right(updatedPrefs))
  }
}
