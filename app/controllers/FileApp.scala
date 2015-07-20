package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}



class FileApp @Inject() (userDAO: dao.UserDAO)(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType
      println(filename+":filename")
      println(contentType+":contentType")
      picture.ref.moveTo(new File(s"/tmp/picture/$filename"))
      Ok("File uploaded")
    }.getOrElse {
      Redirect(routes.Application.index).flashing(
        "error" -> "Missing file")
    }
  }

  def uploadForm = Action {
    Ok(views.html.file.uploadForm())
  }

}
