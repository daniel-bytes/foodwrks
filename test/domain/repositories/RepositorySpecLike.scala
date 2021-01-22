package domain.repositories

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{DiagrammedAssertions, Inside, MustMatchers, OptionValues, WordSpecLike}
import org.scalatestplus.play.WsScalaTestClient
import play.api.db.{Database, Databases}
import play.api.db.evolutions.Evolutions

abstract class RepositorySpecLike
  extends TestKit(ActorSystem("TestActorSystem"))
    with WordSpecLike
    with MustMatchers
    with OptionValues
    with ScalaFutures
    with Inside
    with TypeCheckedTripleEquals
    with DiagrammedAssertions
    with WsScalaTestClient
{
  def withDatabase[T](block: Database => T) = {
    println("Creating database")
    Databases.withDatabase(
      driver = "org.postgresql.Driver",
      url = "postgres://postgres:password@localhost/test"
    ) { database =>
      println("Created database")
      println("Running Evolutions")
      Evolutions.withEvolutions(database) {
        println("Ran Evolutions")
        block(database)
        println("Done with database")
      }
    }
  }
}
