package domain.models

trait PagedSeq[T] {
  def data: Seq[T]
}

case class PageCursor(value: String) extends AnyVal

case class CursorPagedSeq[T](
  data: Seq[T],
  cursor: Option[PageCursor]
) extends PagedSeq[T]

object CursorPagedSeq {
  def empty[T]: CursorPagedSeq[T] = CursorPagedSeq(Seq.empty, None)
}