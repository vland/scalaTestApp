package db

import com.typesafe.config.ConfigFactory
import db.entity.{WordInfo, WordLib}
import org.slf4j.LoggerFactory
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.{Session, SessionFactory}
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

    SessionFactory.concreteFactory = Some(()=>
        Session.create(
          java.sql.DriverManager.getConnection(
            DbSettings.connectionString,
            DbSettings.user,
            DbSettings.password),
    new MySQLAdapter))

    try {
      transaction {
        WordLib.wordsLib
          .where(w => w.name === name)
          .headOption
      }
    } catch {
      case e => {
        logger.error(e.toString, e)
        None
      }
    }
  }

  def insertWord(word: WordInfo) : Unit = {

    SessionFactory.concreteFactory = Some(()=>
      Session.create(
        java.sql.DriverManager.getConnection(
          DbSettings.connectionString,
          DbSettings.user,
          DbSettings.password),
        new MySQLAdapter))

    try {
      transaction {
        WordLib.wordsLib.insert(word)
      }
    } catch {
      case e => logger.error(e.toString, e)
    }
  }
}
