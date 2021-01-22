import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.crypto.{AuthenticatorEncoder, Base64AuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.services.{AuthenticatorService, IdentityService}
import com.mohiva.play.silhouette.api.util.{Clock, FingerprintGenerator, HTTPLayer, IDGenerator, PasswordInfo, PlayHTTPLayer}
import com.mohiva.play.silhouette.crypto.{JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators.{SessionAuthenticator, SessionAuthenticatorService, SessionAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.{DefaultSocialStateHandler, OAuth1Info, OAuth2Info, OAuth2Settings, OpenIDInfo, SocialStateHandler}
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import com.mohiva.play.silhouette.impl.providers.state.{CsrfStateItemHandler, CsrfStateSettings}
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import controllers.Auth
import controllers.Auth._
import domain.repositories.PlacesSearchRepository.GooglePlacesConfig
import domain.repositories.{CommentsRepository, PlacesRepository, PlacesSearchRepository, UsersRepository}
import domain.services._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.SessionCookieBaker

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

// See https://github.com/mohiva/play-silhouette-seed/blob/master/app/modules/SilhouetteModule.scala
// for the Silhouette configuration

class Module extends AbstractModule with ScalaModule {
  override def configure() = {
    bind[Clock].toInstance(Clock())
    bind[EventBus].toInstance(EventBus())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator())
    bind[AuthenticatorEncoder].toInstance(new Base64AuthenticatorEncoder)
    bind[AuthenticatorService[SessionAuthenticator]].to[SessionAuthenticatorService]

    bind[Silhouette[SessionEnv]]
      .to[SilhouetteProvider[SessionEnv]]

    bind[IdentityService[SessionEnv#I]]
      .to[Auth.UserIdentityService]

    bind[IdentityService[UserIdentity]]
      .to[UserIdentityService]

    bind[CommentsService]
      .to[CommentsService.Default]

    bind[PlacesService]
      .to[PlacesService.Default]

    bind[UsersService]
      .to[UsersService.Default]

    bind[CommentsRepository]
      .to[CommentsRepository.SqlDatabase]

    bind[PlacesRepository]
      .to[PlacesRepository.SqlDatabase]

    bind[PlacesSearchRepository]
      .to[PlacesSearchRepository.GooglePlacesSearchRepository]

    bind[UsersRepository]
      .to[UsersRepository.SqlDatabase]
  }

  @Provides
  def provideGooglePlacesConfig(config: Configuration): GooglePlacesConfig =
    GooglePlacesConfig(
      apiKey = config.get[String]("apis.google.places.apiKey")
    )

  @Provides
  def provideIdGenerator()(implicit ec: ExecutionContext): IDGenerator =
    new SecureRandomIDGenerator()

  @Provides
  def provideHTTPLayer(client: WSClient)(implicit ec: ExecutionContext): HTTPLayer =
    new PlayHTTPLayer(client)

  @Provides @Named("social-state-signer")
  def provideSocialStateSigner(config: Configuration): Signer =
    new JcaSigner(
      JcaSignerSettings(
        key = config.get[String]("silhouette.socialStateHandler.signer.key")
      )
    )

  @Provides
  def provideSocialStateHandler(
    @Named("social-state-signer") signer: Signer,
    csrfStateItemHandler: CsrfStateItemHandler
  ): SocialStateHandler =
    new DefaultSocialStateHandler(Set(csrfStateItemHandler), signer)

  @Provides @Named("csrf-state-item-signer")
  def provideCSRFStateItemSigner(config: Configuration): Signer =
    new JcaSigner(
      JcaSignerSettings(
        key = config.get[String]("silhouette.socialStateHandler.signer.key")
      )
    )

  @Provides
  def provideCsrfStateItemHandler(
    idGenerator: IDGenerator,
    @Named("csrf-state-item-signer") signer: Signer,
    config: Configuration
  ): CsrfStateItemHandler = {
    new CsrfStateItemHandler(
      CsrfStateSettings(
        cookieName = config.get[String]("silhouette.csrfStateItemHandler.cookieName"),
        cookiePath = config.get[String]("silhouette.csrfStateItemHandler.cookiePath"),
        secureCookie = config.get[Boolean]("silhouette.csrfStateItemHandler.secureCookie")
      ),
      idGenerator,
      signer
    )
  }

  @Provides
  def provideGoogleProvider(
    httpLayer: HTTPLayer,
    socialStateHandler: SocialStateHandler,
    config: Configuration
  ): GoogleProvider =
    new GoogleProvider(
      httpLayer,
      socialStateHandler,
      OAuth2Settings(
        authorizationURL = config.getOptional[String]("silhouette.google.authorizationURL"),
        accessTokenURL = config.get[String]("silhouette.google.accessTokenURL"),
        redirectURL = config.getOptional[String]("silhouette.google.redirectURL"),
        clientID = config.get[String]("silhouette.google.clientID"),
        clientSecret = config.get[String]("silhouette.google.clientSecret"),
        scope = config.getOptional[String]("silhouette.google.scope"),
      )
    )

  @Provides
  def provideAuthenticatorService(
    config: Configuration,
    fingerprintGenerator: FingerprintGenerator,
    authenticatorEncoder: AuthenticatorEncoder,
    sessionCookieBaker: SessionCookieBaker,
    clock: Clock
  )(implicit ec: ExecutionContext): SessionAuthenticatorService =
    new SessionAuthenticatorService(
      SessionAuthenticatorSettings(
        sessionKey = config.get[String]("silhouette.authenticator.sessionKey"),
        useFingerprinting = config.get[Boolean]("silhouette.authenticator.useFingerprinting"),
        authenticatorIdleTimeout = config.getOptional[FiniteDuration]("silhouette.authenticator.authenticatorIdleTimeout"),
        authenticatorExpiry = config.get[FiniteDuration]("silhouette.authenticator.authenticatorExpiry")
      ),
      fingerprintGenerator,
      authenticatorEncoder,
      sessionCookieBaker,
      clock
    )

  @Provides
  def provideEnvironment(
    userService: UserIdentityService,
    authenticatorService: AuthenticatorService[SessionAuthenticator],
    eventBus: EventBus
  )(implicit ec: ExecutionContext): Environment[SessionEnv] =
    Environment[SessionEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
}
