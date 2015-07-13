package controllers

import dao.CatDAO
import dao.DogDAO
import javax.inject.Inject
import models.Cat
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.text
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller
import models.Dog
import play.api.i18n.Lang
import play.api.i18n.{ MessagesApi, I18nSupport }
import javax.inject.Provider
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
