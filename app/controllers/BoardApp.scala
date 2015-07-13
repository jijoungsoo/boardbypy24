package controllers;

import java.io.IOException
import java.util.List
import java.util.Objects
import play.api.i18n.I18nSupport
import models._
import models.enumeration.UserState
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.StringUtils
import com.fasterxml.jackson.databind.node.ObjectNode
import play.Configuration
import play.Logger
import play.Play
import play.i18n.Messages
import play.libs.Json
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
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import dao.BoardDAO
import java.util.Date

case class InsertBoardData(content: String)
case class BoardData(content: String, writerIp: String, writerId: Long)
class BoardApp @Inject() (boardDAO: dao.BoardDAO)(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def posts(pageNum: Int) = Action.async {
    implicit request =>
    boardDAO.all().map { boards => Ok(views.html.board.list(boards)) }
  }

  val boardForm = Form(
    mapping(
      "content" -> play.api.data.Forms.nonEmptyText)(InsertBoardData.apply)(InsertBoardData.unapply))

  def newPostForm = Action {
    Ok(views.html.board.create(boardForm))
  }

  def newPost = Action {
    implicit request =>
      Logger.debug("newPost : ")
      boardForm.bindFromRequest.fold(
        formWithErrors => {
          Logger.debug("newPost : error ")
          // binding failure, you retrieve the form containing errors:
          BadRequest(views.html.board.create(formWithErrors))

        },
        InsertBoardData => {
          Logger.debug("newPost : ok ")
          /* binding success, you get the actual value. */
          
          

          val boardDate1 = BoardData(InsertBoardData.content,request.remoteAddress, 23)
          val id = boardDAO.insert(boardDate1)
          //Redirect(routes.Application.index)
          Redirect(controllers.routes.BoardApp.posts(1))

        })

  }

  def commitReadmeFile = TODO
  def post(number: Long) = TODO

  var editPostForm1 = Form(
    mapping(
      "id" -> play.api.data.Forms.longNumber,
      "content" -> play.api.data.Forms.nonEmptyText,
      "writerDate" -> play.api.data.Forms.date,
      "writerId" -> play.api.data.Forms.longNumber,
      "writerIp" -> play.api.data.Forms.nonEmptyText)(Board.apply)(Board.unapply))

  def editPostForm(number: Long) = Action {
    implicit request =>
      Logger.debug("editPostForm")
      Logger.debug("number : " + number)
      val board1: scala.concurrent.Future[Seq[models.Board]] = boardDAO.findById(number);
      val result1 = Await.result(board1, Duration.Inf)
      board1.value.get match {
        case Success(s) =>
          if (s.length == 1) {

            val editPostForm2 = editPostForm1.fill(s(0))
            Ok(views.html.board.edit(editPostForm2))
          } else {
            Ok("NOk")
          }
        case Failure(e) => Ok("NOk")
      }
  }
  def editPost(number: Long) = Action {
    implicit request =>
      //val userData: UserData = userForm.bindFromRequest.get
      Logger.debug("editPost")
      editPostForm1.bindFromRequest.fold(
        formWithErrors => {
          Logger.debug("editPost :error")
          BadRequest(views.html.board.edit(formWithErrors))

        },
        editData1 => {
          Logger.debug("editPost :ok")
          /* binding success, you get the actual value. */
          val id = boardDAO.update(editData1)
          Redirect(controllers.routes.BoardApp.posts(1))
          //Ok("Ok")

        })
  }
  def unmarkAnotherReadmePostingIfExists(postingNumber: Long) = TODO
  def deletePost(number: Long) = Action {
    implicit request =>
      Logger.debug("deletePost")
      Logger.debug("number : " + number)

      val board1 = boardDAO.delete(number);
      Redirect(controllers.routes.BoardApp.posts(1))
  }
  def newComment(number: Long) = TODO
  def deleteComment(number: Long, commentId: Long) = TODO
}
