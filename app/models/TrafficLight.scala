package models

import models.Color.Color
import play.api.libs.json.{JsError, JsString, JsSuccess, Json, Reads, Writes}

case class TrafficLight(id: Int, color: Color)

trait TrafficLightJson {
  val RedValue = "Red"
  val OrangeValue = "Orange"
  val GreenValue = "Green"

  implicit val colorReads: Reads[Color] = {
    case JsString(RedValue)    => JsSuccess(Color.Red)
    case JsString(OrangeValue) => JsSuccess(Color.Orange)
    case JsString(GreenValue)  => JsSuccess(Color.Green)
    case _                     => JsError("Not a valid color!")
  }

  implicit val colorWrites: Writes[Color] = {
    case Color.Red    => JsString(RedValue)
    case Color.Orange => JsString(OrangeValue)
    case Color.Green  => JsString(GreenValue)
  }

  implicit val trafficLightFormat = Json.format[TrafficLight]
}