package controllers

import javax.inject.{Inject, Provider}

import dao.{CatDAO, DogDAO}
import models.{Cat, Dog}
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import play.api.routing.Router

class Application @Inject() (catDao: CatDAO, dogDao: DogDAO)(val messagesApi: MessagesApi)(router: Provider[Router]) extends Controller with I18nSupport {

  def sitemap = Action {
    Ok(views.html.sitemap(Some(router.get)))
  }

  def index = Action.async {

      catDao.all().zip(dogDao.all()).map { case (cats, dogs) => Ok(views.html.index(cats, dogs, catForm)) }
    }

  val catForm = Form(
    mapping(
      "name" -> text(),
      "color" -> text())(Cat.apply)(Cat.unapply))

  val dogForm = Form(
    mapping(
      "name" -> text(),
      "color" -> text())(Dog.apply)(Dog.unapply))

  def insertCat = Action { implicit request =>
    val cat: Cat = catForm.bindFromRequest.get
    /*catDao.insert(cat).map(_ => Redirect(routes.Application.index))*/
    catDao.insert(cat)
    Redirect(routes.Application.index)
  }

  def insertDog = Action.async { implicit request =>
    val dog: Dog = dogForm.bindFromRequest.get
    dogDao.insert(dog).map(_ => Redirect(routes.Application.index))
  }

}
