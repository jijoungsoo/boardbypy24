package controllers;

import models._
import models.enumeration.UserState
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.StringUtils
import com.fasterxml.jackson.databind.node.ObjectNode
import play.Configuration
import play.Logger
import play.Play
import play.i18n.Messages

import dao.CatDAO
import dao.DogDAO
import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.text
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.i18n.Lang
import play.api.i18n.{ MessagesApi, I18nSupport }
import play.api.mvc._
import play.mvc.Http
import play.api.libs.iteratee.Enumerator

import models.User
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import play.api.libs.json.Json


case class UserData(loginId: String, name: String, email: String, password: String, retypedPassword: String)
object UserData{
  implicit val fmt = play.api.libs.json.Json.format[UserData]
}


case class LoginData(loginId: String, password: String)

object UserApp {
  val SESSION_UID: String = "UID";
  val SESSION_LOGINID: String = "loginId";
  val SESSION_USERNAME: String = "userName";
  val TOKEN: String = "yobi.token";
  val TOKEN_SEPARATOR: String = ":";
  val TOKEN_LENGTH: Integer = 2;
  val MAX_AGE: Integer = 30 * 24 * 60 * 60;
  val DEFAULT_AVATAR_URL: String = routes.Assets.versioned("images/default-avatar-128.png").url;
  val AVATAR_FILE_LIMIT_SIZE = 1024 * 1000 * 1; //1M
  val MAX_FETCH_USERS: Integer = 1000;
  val HASH_ITERATIONS: Integer = 1024;
  val DAYS_AGO: Integer = 7;
  val UNDEFINED: Integer = 0;
  val DAYS_AGO_COOKIE: String = "daysAgo";
  val DEFAULT_GROUP: String = "own";
  val DEFAULT_SELECTED_TAB: String = "projects";
  val TOKEN_USER: String = "TOKEN_USER";

  def getUserFromSession(implicit request: Request[AnyContent]): User = {

    request.session.get(SESSION_UID) match {
      case Some(uid) =>
        println(uid)
        val jsonNode = Json.parse(uid);
     //   val t: User = Json.fromJson(jsonNode, classOf[User]);
           val t: User =null;
        if (uid == null) {
          return invalidSession;
        }
        if (t == null) {
          return invalidSession;
        }
        return t;
      case None => println("That didn't work.")
      return invalidSession;
    }

  }

  def invalidSession(implicit request: Request[AnyContent]): User = {

    val date = new java.util.Date();
    val sqlDate = new java.sql.Date(date.getTime());
    return User(-1l, "존재하지 않는 사용자입니다.", "", "", "", "", Some(""), "F", sqlDate);
  }

  def currentUser(implicit request: Request[AnyContent]): User = {
    val user: User = getUserFromSession;
    if (user.id > 0) {
      return user;
    }
    return invalidSession;
  }
  def isLoggedInSession(implicit request: Request[AnyContent]): Boolean =
    {
      val user = getUserFromSession
      if (user.id > 0) {
        return true;
      } else {
        return false;
      }
    }
  
  
}

class UserApp @Inject() (userDAO: dao.UserDAO)(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def users = Action.async {
    userDAO.all().map { users => Ok(views.html.user.users(users)) }

  }

  val loginFormParam = Form(
    mapping(
      "loginId" -> play.api.data.Forms.nonEmptyText,
      "password" -> play.api.data.Forms.nonEmptyText)(LoginData.apply)(LoginData.unapply))

  def loginForm = Action {
    implicit request =>

      Ok(views.html.user.login(loginFormParam))
  }

  def logout = TODO
  def login = Action {
    implicit request =>
      loginFormParam.bindFromRequest.fold(
        formWithErrors => {
          // binding failure, you retrieve the form containing errors:
          BadRequest(views.html.user.login(formWithErrors))

        },
        loginData => {
          /* binding success, you get the actual value. */
          val users: scala.concurrent.Future[Seq[models.User]] = userDAO.findByLoginId(loginData.loginId)
          val result: Seq[models.User] = Await.result(users, Duration.Inf)

          val result1 = result.filter { x => x.password == loginData.password }
          if (result1.length > 0) {
            //request.session.+("loginId" -> loginData.loginId)
            Ok("OK").withSession("loginId" -> loginData.loginId).withCookies(Cookie("theme", "blue"))
 

var test =  UserData("asdf","qwe","er","er","cv")
Logger.debug("aaa")

Logger.debug(Json.prettyPrint(Json.toJson(test)))
Logger.debug("bbbb")

val answe = Json.toJson[models.User](result1(0))
        Logger.debug("answe : "+answe)
               Logger.debug("Json.prettyPrint(answe) : "+Json.prettyPrint(answe))
            
          //  Ok("OK").withSession(UserApp.SESSION_UID -> Json.prettyPrint(answe))
      Ok("OK").withSession("loginId" -> loginData.loginId).withCookies(Cookie("theme", "blue"))
          } else {
            BadRequest(views.html.user.login(loginFormParam))
          }
        })
  }

  val userForm = Form(
    mapping(
      "loginId" -> play.api.data.Forms.nonEmptyText,
      "name" -> play.api.data.Forms.nonEmptyText,
      "email" -> play.api.data.Forms.nonEmptyText,
      "password" -> play.api.data.Forms.nonEmptyText,
      "retypedPassword" -> play.api.data.Forms.nonEmptyText)(UserData.apply)(UserData.unapply))

  def signupForm = Action {
    Ok(views.html.user.signup(userForm))

  }

  def signup = Action {
    implicit request =>
      //val userData: UserData = userForm.bindFromRequest.get
      Ok("todo")
      userForm.bindFromRequest.fold(
        formWithErrors => {
          // binding failure, you retrieve the form containing errors:
          BadRequest(views.html.user.signup(formWithErrors))

        },
        userData1 => {
          /* binding success, you get the actual value. */
          val id = userDAO.insert(userData1)
          //Redirect(routes.Application.index)
          Redirect(controllers.routes.UserApp.users)

        })
  }



  var userEditForm = Form(
    mapping(
      "id" -> play.api.data.Forms.longNumber,
      "name" -> play.api.data.Forms.nonEmptyText,
      "loginId" -> play.api.data.Forms.nonEmptyText,
      "password" -> play.api.data.Forms.nonEmptyText,
      "passwordSalt" -> play.api.data.Forms.nonEmptyText,
      "email" -> play.api.data.Forms.nonEmptyText,
      "avatarUrl" -> optional(play.api.data.Forms.text),
      "remeberMe" -> play.api.data.Forms.text,
      "date" -> play.api.data.Forms.date)(User.apply)(User.unapply))

  def editUserInfoForm = Action {
    implicit request =>
      Logger.debug("editUserInfoForm")
      val uid: String = request.body.asFormUrlEncoded.get("id")(0)
      Logger.debug("uid : " + uid)

      val user1: scala.concurrent.Future[Seq[models.User]] = userDAO.findById(uid.toLong);

      val result1 = Await.result(user1, Duration.Inf)
      user1.value.get match {
        case Success(s) =>
          if (s.length == 1) {

            val userEditForm1 = userEditForm.fill(s(0))
            Ok(views.html.user.edit(userEditForm1))
          } else {
            Ok("NOk")
          }
        case Failure(e) => Ok("NOk")
      }

  }
  def editUserInfoByTabForm(tabId: String) = TODO
  def editUserInfo = Action {
    implicit request =>
      //val userData: UserData = userForm.bindFromRequest.get
      Ok("todo")
      userEditForm.bindFromRequest.fold(
        formWithErrors => {
          // binding failure, you retrieve the form containing errors:
          BadRequest(views.html.user.edit(formWithErrors))

        },
        userData1 => {
          /* binding success, you get the actual value. */
          val id = userDAO.update(userData1)
          Redirect(controllers.routes.UserApp.users)
          //Ok("Ok")

        })
  }
  def leave = Action {
    implicit request =>
      Logger.debug("leave")
      val uid: String = request.body.asFormUrlEncoded.get("id")(0)
      Logger.debug("uid : " + uid)

      val user1 = userDAO.delete(uid.toLong);
      Redirect(controllers.routes.UserApp.users)
  }

  def resetUserPassword = TODO

  def isUsed(name: String) = TODO
  def isEmailExist(email: String) = TODO
  def sendValidationEmail(emailId: Long) = TODO
  def confirmEmail(emailId: Long, token: String) = TODO


}
