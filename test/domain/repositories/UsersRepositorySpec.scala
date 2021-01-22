package domain.repositories

import akka.actor.ActorSystem
import akka.testkit.TestKit
import domain.models.{User, ExternalUserId, UserNotFoundException}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.db.evolutions.Evolutions
import play.api.db.{Database, Databases}
import play.api.test.Injecting

class UsersRepositorySpec extends RepositorySpecLike {
  def withRepository[T](block: UsersRepository => T) = {
    withDatabase { database =>
      println("Creating UsersRepository")
      block(
        new UsersRepository.SqlDatabase(
          database,
          new domain.repositories.DatabaseExecutionContext(system)
        )
      )
      println("Done with UsersRepository")
    }
  }

  "UsersRepository#SqlDatabase" should {
    "getUser" should {
      "return NotFound when the User does not exist" in {
        withRepository { repository =>
          val userId = ExternalUserId("unknown", "notfound")

          inside(repository.getUserByExternalId(userId).futureValue) {
            case Left(
              domain.Error.NotFoundError(UserNotFoundException(id))
            ) => assert(id === userId.toString)
          }
        }
      }
    }
  }
}
