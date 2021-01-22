package domain

import akka.actor.ActorSystem
import com.google.inject._
import play.api.libs.concurrent.CustomExecutionContext

import java.sql.ResultSet

package object repositories {
  trait Repository extends Service

  @Singleton
  class DatabaseExecutionContext @Inject()(system: ActorSystem) extends CustomExecutionContext(system, "database.dispatcher")

  implicit class ResultSetSyntax(val resultSet: ResultSet) extends AnyVal {
    def map[T](f: ResultSet => T): Iterator[T] = {
      new Iterator[T] {
        def hasNext = resultSet.next()
        def next() = f(resultSet)
      }
    }

    def getOpt[T](f: ResultSet => T): Option[T] = {
      if (resultSet.next()) {
        Some(f(resultSet))
      } else None
    }
  }
}
