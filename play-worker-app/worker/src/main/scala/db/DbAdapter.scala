package db

import com.typesafe.config.ConfigFactory
import db.entity.WordInfo
import db.entity.WordLib
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.{Session, SessionFactory}
import org.squeryl.PrimitiveTypeMode._

object DbSettings {
  val connectionString = ConfigFactory.load().getString("wordLibDb.connectionString")
  val user = ConfigFactory.load().getString("wordLibDb.user")
  val password = ConfigFactory.load().getString("wordLibDb.password")
}

class DbAdapter {
  def findWord(name: String) : Option[WordInfo] = {
    // TODO: make some refactoring
    // TODO: possible use SessionFactory and transaction
    var result: Option[WordInfo] = None
    var session: Session = null
    try {
      session = Session.create(java.sql.DriverManager.getConnection(DbSettings.connectionString, DbSettings.user, DbSettings.password), new PostgreSqlAdapter)

      using(session) {
        result = WordLib.wordsLib.where(w => w.name === name).headOption
      }
    }
    finally {
      session.close
    }

    result
  }

  def insertWord(name: String, value: String) : Unit = {
    // TODO: make some refactoring
    // TODO: possible use SessionFactory and transaction
    var session: Session = null
    try {
      session = Session.create(java.sql.DriverManager.getConnection(DbSettings.connectionString, DbSettings.user, DbSettings.password), new PostgreSqlAdapter)

      using(session) {
        WordLib.wordsLib.insert(new WordInfo(name, value))
      }
    }
    catch {
      case _: Exception => session.connection.rollback()
    }
    finally {
      session.close
    }
  }
}
