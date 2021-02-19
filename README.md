# foodwrks

"Foodwrks" is a simple server-rendered CRUD app for searching and commenting on local restaraunts and businesses.  It originated out of my wife and I's desire to leave private comments and notes on Yelp and Seamless, and serves as a portfolio piece for web / CRUD app code.  It is built using Scala and the [Play framework](https://www.playframework.com/), with [PostgreSQL](https://www.postgresql.org/) and the [Google Places API](https://developers.google.com/places/web-service/overview).  The UI uses [Bulma](https://bulma.io/) for CSS and vanilla Javascript for added interations and dynamic forms.

## Architecture

Built on top of the Play framework, the top level application code is fairly straightforward MVC setup, with an additional `domain` layer for core business logic:

- `models`: These are the web UI layer models, including Play form models
- `controllers`: The controller layer, with each class representing a URL scope and each function a page or form action, with authentication logic via [Silhouette](https://www.silhouette.rocks)
- `views`: Twirl HTML views, server rendered
- `public`: Public assets such as images and Javascript
- `domain`: The application domain logic
- `conf`: Configuration files, including the database schema migrations via Play framework [evolutions](https://www.playframework.com/documentation/2.8.x/Evolutions)

### Domain layer

The domain layer is composed of services, repositories and models.

- `models`: The core domain model layer, where all application data is modelled.  Note that this is not a 1:1 direct mapping of database tables.
- `repositories`: The data access layer, encapsulating access to both the PostgreSQL database and the Google Places REST API
- `services`: The business logic layer, which delegates data access logic to the repositories layer. Services can depend on multiple repositories but do not depend on other services.

For database access vanilla JDBC is used to interact with PostgreSQL. No direct table or views are accessed via the code, only stored procedures (for writes) and functions (for reads) are to be accessed.  This keeps the SQL code in actual SQL files and ensures that the database tables can evolve while still accepting and returning data in the shape expected by the domain model layer.

The Google Places API is encapsulated via the `GooglePlacesSearchRepository` class, which implements the generic `PlacesSearchRepository` trait.  Internally, case class data models are defined to represent the expected REST API response, and these are mapped to the domain model layer and returned from the various methods.
