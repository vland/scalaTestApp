package db

import com.typesafe.config.ConfigFactory
import db.entity.{WordInfo, WordLib}
import org.slf4j.LoggerFactory
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.Session
import org.squeryl.PrimitiveTypeMode._

object DbSettings {
  val connectionString = ConfigFactory.load()
    .getString("db.default.url")

  val user = ConfigFactory.load()
    .getString("db.default.username")

  val password = ConfigFactory.load()
    .getString("db.default.password")
}

class DbAdapter {
  val logger = LoggerFactory.getLogger(classOf[DbAdapter])

  def findWord(name: String) : Option[WordInfo] = {
    var result: Option[WordInfo] = None
    var session: Session = null
    try {
      session =
        Session.create(
          java.sql.DriverManager.getConnection(
            DbSettings.connectionString,
            DbSettings.user,
            DbSettings.password),
          new MySQLAdapter)

      using(session) {
        result = WordLib.wordsLib
          .where(w => w.name === name)
          .headOption
      }
    } catch {
      case e: Exception => {
        logger.error(e.toString, e)
      }
    }
    finally {
      session.close
    }

    result
  }

  def insertWord(word: WordInfo) : Unit = {
    var session: Session = null
    try {
      session =
        Session.create(
          java.sql.DriverManager.getConnection(
            DbSettings.connectionString,
            DbSettings.user,
            DbSettings.password),
          new MySQLAdapter)

      using(session) {
        WordLib.wordsLib.insert(word)
      }
    }
    catch {
      case e: Exception => {
        if(session != null) {
          session.connection.rollback()
        }
        logger.error(e.toString, e)
      }
    }
    finally {
      session.close
    }
  }
}
