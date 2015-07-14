package models

import java.util.Date
import play.api.libs.json.Json

case class User(
    id:Long
    ,name: String
    ,loginId: String
    ,password: String
    ,passwordSalt: Option[String]
    ,email: String
    ,avatarUrl: Option[String]
    ,remeberMe: Option[String]
    ,date: Date
)
object User{
  implicit val fmt = play.api.libs.json.Json.format[User]
}