package dao;

import scala.concurrent.Future
import javax.inject.Inject
import models.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import play.Logger
import scala.util.{ Try, Success, Failure }
import java.util.Date

class UserDAO @Inject() (@NamedDatabase("default") protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Users = TableQuery[UserTable]

  def all(): Future[Seq[User]] = db.run(Users.result)

  /*def insert(user: User): Future[Unit] = db.run(Users += user).map { _ => () }*/
  /*
   var tmp: DBIO[Int] = sqlu"insert into TB_USER (NAME,LOGIN_ID,PASSWORD,EMAIL) values (${c.name}, ${c.loginId}, ${c.password}, ${c.email})"
    val affectedRowsCount: Future[Int] = db.run(tmp);
      * */
  def findById(uid: Long): Future[Seq[User]] = {
    Logger.debug("findbyId")
    val q = Users.filter(_.id === uid)
    val action1 = q.result
    val sql = action1.statements.head
    Logger.debug("findbyId sql : " + sql)
    return db.run(action1);
  }
  
    def findByLoginId(loginId: String): Future[Seq[User]] = {
    Logger.debug("findbyId")
    val q = Users.filter(_.loginId === loginId)
    val action1 = q.result
    val sql = action1.statements.head
    Logger.debug("findByLoginId sql : " + sql)
    return db.run(action1);
  }

  def insert(c: controllers.UserData) = {
    Logger.debug("insert")

    val affectedRowsCount: Future[Int] = db.run {
      Users.map(p => (p.name, p.loginId, p.password, p.email)) += (c.name, c.loginId, c.password, c.email)
    }
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
  }

  def delete(uid: Long) = {
    Logger.debug("delete")
    val q = Users.filter(_.id === uid).delete
    val sql = q.statements.head
    Logger.debug("delete sql : " + sql)
    db.run(q);
  }

  def update(c: models.User) = {
    Logger.debug("update")

    val q = Users.filter(_.id === c.id)
      .map(p => (p.loginId, p.name, p.password, p.email))
      .update((c.loginId,c.name, c.password, c.email))
    val sql = q.statements.head
    Logger.debug("update sql : " + sql)
    db.run(q)

  }

  implicit val DateTimeColumeType = MappedColumnType.base[java.util.Date, java.sql.Timestamp](

    { d => java.sql.Timestamp.from(d.toInstant()) },
    { t => Date.from(t.toInstant()) })

  private class UserTable(tag: Tag) extends Table[User](tag, "TB_USER") {

    def id = column[Long]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def loginId = column[String]("LOGIN_ID")
    def password = column[String]("PASSWORD")
    def passwordSalt = column[String]("PASSWORD_SALT")
    def email = column[String]("EMAIL")
    def avatarUrl = column[Option[String]]("AVATAR_URL")
    def remeberMe = column[String]("REMEMBER_ME")
    def date = column[Date]("DATE")
    def * = (id, name, loginId, password, passwordSalt, email, avatarUrl, remeberMe, date) <> ((User.apply _).tupled, User.unapply _)
  }
  
}