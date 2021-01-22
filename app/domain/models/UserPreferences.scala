package domain.models

import play.api.libs.json._

import scala.util.{Failure, Try}

sealed trait UserPreferenceValue
case class NumberUserPreferenceValue(value: BigDecimal) extends UserPreferenceValue
case class BooleanUserPreferenceValue(value: Boolean) extends UserPreferenceValue
case class StringUserPreferenceValue(value: String) extends UserPreferenceValue

case class UserPreferences(values: Map[String, UserPreferenceValue]) {
  def getInt(key: String): Int = values.get(key) match {
    case Some(NumberUserPreferenceValue(value)) => value.toInt
    case Some(StringUserPreferenceValue(value)) => value.toIntOption.getOrElse(0)
    case _ => 0
  }

  def getLong(key: String): Long = values.get(key) match {
    case Some(NumberUserPreferenceValue(value)) => value.toLong
    case Some(StringUserPreferenceValue(value)) => value.toLongOption.getOrElse(0)
    case _ => 0
  }

  def getDouble(key: String): Double = values.get(key) match {
    case Some(NumberUserPreferenceValue(value)) => value.toDouble
    case Some(StringUserPreferenceValue(value)) => value.toDoubleOption.getOrElse(0)
    case _ => 0
  }

  def getBoolean(key: String): Boolean = values.get(key) match {
    case Some(NumberUserPreferenceValue(value)) => value != 0
    case Some(BooleanUserPreferenceValue(value)) => value
    case Some(StringUserPreferenceValue(value)) => value.toBooleanOption.getOrElse(false)
    case _ => false
  }

  def getString(key: String): String = values.get(key) match {
    case Some(StringUserPreferenceValue(value)) => value
    case Some(pref) => pref.toString
    case _ => ""
  }

  def toJsonString: String = {
    import UserPreferencesJsonSupport._
    userPreferencesWrites.writes(this).toString()
  }
}

object UserPreferences {
  val empty = UserPreferences(Map.empty[String, UserPreferenceValue])

  def apply(json: String): UserPreferences = {
    import UserPreferencesJsonSupport._
    userPreferencesReads.reads(Json.parse(json)).getOrElse(empty)
  }
}

object UserPreferencesJsonSupport {
  implicit val userPreferenceValueReads = new Reads[UserPreferenceValue] {
    override def reads(json: JsValue): JsResult[UserPreferenceValue] = {
      val result: Try[UserPreferenceValue] = json match {
        case JsNumber(number) => Try(NumberUserPreferenceValue(number))
        case JsBoolean(bool) => Try(BooleanUserPreferenceValue(bool))
        case JsString(str) => Try(StringUserPreferenceValue(str))
        case err => Failure(JsResult.Exception(JsError(s"Failed to parse JSON field [$err]")))
      }
      JsResult.fromTry(result)
    }
  }
  implicit val userPreferenceValueWrites = new Writes[UserPreferenceValue] {
    override def writes(o: UserPreferenceValue): JsValue = o match {
      case NumberUserPreferenceValue(number) => JsNumber(number)
      case BooleanUserPreferenceValue(bool) => JsBoolean(bool)
      case StringUserPreferenceValue(str) => JsString(str)
    }
  }
  implicit val userPreferencesReads = Json.reads[UserPreferences]
  implicit val userPreferencesWrites = Json.writes[UserPreferences]
}
