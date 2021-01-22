import cats.data.{EitherT, NonEmptyList}

import scala.concurrent.{ExecutionContext, Future}

package object domain {
  sealed trait DomainException extends Throwable

  object DomainException {
    trait NotFoundException extends DomainException

    case class DeserializationException(messages: Seq[String])
      extends Exception(messages.mkString(", "))
        with DomainException

    class BackendException(message: String, cause: Throwable)
      extends Exception(message, cause)
        with DomainException {
      def this(message: String) = {
        this(message, null)
      }
    }

    case class AggregateException(errors: NonEmptyList[DomainException])
      extends Exception(errors.map(_.getLocalizedMessage).toList.mkString(", "))
        with DomainException
  }

  sealed trait Error {
    def toDomainException: DomainException
  }

  object Error {
    case class NotFoundError(ex: DomainException.NotFoundException) extends Error {
      override def toDomainException: DomainException = ex
    }

    case class DeserializationError(messages: Seq[String]) extends Error {
      // override as val so we capture the stack trace at time of creation
      override val toDomainException: DomainException =
        DomainException.DeserializationException(messages)
    }

    case class BackendError(error: DomainException.BackendException) extends Error {
      override def toDomainException: DomainException = error
    }

    case class AggregateError(errors: NonEmptyList[Error]) extends Error {
      // override as val so we capture the stack trace at time of creation
      override val toDomainException: DomainException =
        DomainException.AggregateException(errors.map(_.toDomainException))
    }
  }

  implicit class DomainAsyncResultSyntax[T](val result: Service#AsyncResult[T]) extends AnyVal {
    def toEitherT(implicit ec: ExecutionContext): EitherT[Future, Error, T] =
      EitherT(result)
  }

  implicit class ResultSequenceSyntax[T](val results: Seq[Service#Result[T]]) extends AnyVal {
    def foldResults: Service#Result[Seq[T]] = {
      results.foldLeft[Service#Result[Seq[T]]](Right(Seq.empty)) {
        // Both accumulator and error are aggregates, merge
        case (Left(Error.AggregateError(acc)), Left(Error.AggregateError(errors))) =>
          Left(Error.AggregateError(acc ++ errors.toList))

        // Accumulator is aggregate, append new error
        case (Left(Error.AggregateError(acc)), Left(err)) =>
          Left(Error.AggregateError(acc :+ err))

        // Error is aggregate, append previous error
        case (Left(error), Left(Error.AggregateError(agg))) =>
          Left(Error.AggregateError(agg :+ error))

        // Both standard errors, turn into aggregate
        case (Left(acc), Left(error)) =>
          Left(Error.AggregateError(NonEmptyList(acc, error :: Nil)))

        // Has previous errors, ignore result
        case (Left(acc), Right(_)) => Left(acc)

        // Has new error, return it
        case (Right(_), Left(err)) => Left(err)

        // Success! Merge results
        case (Right(acc), Right(value)) => Right(acc :+ value)
      }
    }
  }

  trait Service {
    type Result[T] = Either[Error, T]
    type AsyncResult[T] = Future[Result[T]]
  }
}
