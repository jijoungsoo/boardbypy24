package dao

import scala.concurrent.Future
import javax.inject.Inject
import models.Cat
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import play.api.Logger
import scala.util.{ Try, Success, Failure }

class CatDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Cats = TableQuery[CatsTable]

  def all(): Future[Seq[Cat]] = db.run(Cats.result)

  /*def insert(cat: Cat): Future[Unit] = db.run(Cats += cat).map { _ => () }*/

  def insert(c: Cat) = {
    Logger.debug("insert22")
    /*
   var tt:DBIO[Int]=sqlu"insert into CAT (name,color) values (${c.name}, ${c.color})"
   db.run(tt);
   */


    val affectedRowsCount: Future[Int] = db.run(Cats.map(p => (p.name, p.color)) += (c.name, c.color))
    var isCompleted :Boolean =affectedRowsCount.isCompleted;
    Logger.debug("insert isCompleted :" + isCompleted)
    affectedRowsCount.onSuccess {
      case msg =>
        Logger.debug("insert affectedRowsCount :" + msg)
    }
    affectedRowsCount.onFailure {
      case msg =>
        Logger.debug("Failed to import: :" + msg)
    }
    affectedRowsCount.onComplete { value: Try[Int] =>
      println("onComplete: value=" + value)
      value match {
        case Success(s) => println("onComplete: s=" + s)
        case Failure(e) => println("onComplete: e=" + e.getMessage)
      }
    }
    Logger.debug("insert11111")
  }

  private class CatsTable(tag: Tag) extends Table[Cat](tag, "CAT") {

    def name = column[String]("NAME", O.PrimaryKey)
    def color = column[String]("COLOR")

    def * = (name, color) <> (Cat.tupled, Cat.unapply _)
  }
}
