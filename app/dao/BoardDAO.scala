package dao;

import scala.concurrent.Future
import javax.inject.Inject
import models.Board
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import play.Logger
import scala.util.{ Try, Success, Failure }
import java.util.Date

class BoardDAO @Inject() (@NamedDatabase("default") protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val TBoard = TableQuery[BoardTable]

  def all(): Future[Seq[Board]] = db.run(TBoard.result)

  /*def insert(user: User): Future[Unit] = db.run(Users += user).map { _ => () }*/
  /*
   var tmp: DBIO[Int] = sqlu"insert into TB_USER (NAME,LOGIN_ID,PASSWORD,EMAIL) values (${c.name}, ${c.loginId}, ${c.password}, ${c.email})"
    val affectedRowsCount: Future[Int] = db.run(tmp);
      * */
  def findById(uid: Long): Future[Seq[Board]] = {
    Logger.debug("findbyId")
    val q = TBoard.filter(_.id === uid)
    val action1 = q.result
    val sql = action1.statements.head
    Logger.debug("findbyId sql : " + sql)
    return db.run(action1);
  }
  
  def insert(c: controllers.BoardData) = {
    Logger.debug("insert")

    val affectedRowsCount: Future[Int] = db.run {
      TBoard.map(p => (p.content, p.writerId, p.writerIp)) += (c.content, c.writerId, c.writerIp)
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
    val q = TBoard.filter(_.id === uid).delete
    val sql = q.statements.head
    Logger.debug("delete sql : " + sql)
    db.run(q);
  }

  def update(c: models.Board) = {
    Logger.debug("update")

    val q = TBoard.filter(_.id === c.id)
      .map(p => (p.content))
      .update((c.content))
    val sql = q.statements.head
    Logger.debug("update sql : " + sql)
    db.run(q)

  }

  implicit val DateTimeColumeType = MappedColumnType.base[java.util.Date, java.sql.Timestamp](

    { d => java.sql.Timestamp.from(d.toInstant()) },
    { t => Date.from(t.toInstant()) })

  private class BoardTable(tag: Tag) extends Table[Board](tag, "TB_BOARD") {

    def id = column[Long]("ID", O.PrimaryKey)
    def content = column[String]("CONTENT")
    def writerDate = column[Date]("WRITER_DATE")
    def writerId = column[Long]("WRITER_ID")
    def writerIp = column[String]("WRITER_IP")
    
    
    def * = (id, content, writerDate, writerId, writerIp) <> (Board.tupled, Board.unapply _)
  }
}