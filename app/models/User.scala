package models

import java.util.Date
import play.api.libs.json.Json

case class User(
    id:Long
    ,name: String
    ,loginId: String
    ,password: String
    ,passwordSalt: String
    ,email: String
    ,avatarUrl: Option[String]
    ,remeberMe: String
    ,date: Date
)
object User{
  implicit val fmt = play.api.libs.json.Json.format[User]
}