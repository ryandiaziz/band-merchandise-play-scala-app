package utils

import play.api.libs.json.*
import play.api.libs.json.Json.JsValueWrapper

object JsonConfig {
  // Custom Writes for Option to preserve nulls
  def optionWithNull[A: Writes]: Writes[Option[A]] = Writes {
    case Some(value) => Json.toJson(value)
    case None        => JsNull
  }

  // Helper to generate (key -> JsValue) preserving null for Option
  def optionalField[A: Writes](key: String, value: Option[A]): (String, JsValueWrapper) =
    key -> optionWithNull[A].writes(value)
}