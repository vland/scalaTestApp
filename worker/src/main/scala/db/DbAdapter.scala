package db

import com.typesafe.config.ConfigFactory
import db.entity.{ WordInfo, WordLib}
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
  def findWord(name: String) : Option[WordInfo] = {
    // TODO: make some refactoring
    // TODO: possible use SessionFactory and transaction
    var result: Option[WordInfo] = None
    var session: Session = null
    try {
      session = Session.create(
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
        println(e.toString)
      }
    }
    finally {
      session.close
    }

    result
  }

  def insertWord(word: WordInfo) : Unit = {
    // TODO: make some refactoring
    // TODO: possible use SessionFactory and transaction
    var session: Session = null
    try {
      session = Session.create(
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
        println(e.toString)
      }
    }
    finally {
      session.close
    }
  }
}
