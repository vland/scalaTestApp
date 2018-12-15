package db.entity

import org.squeryl.customtypes.{CustomTypesMode, StringField}
import org.squeryl.Schema

class Json(v: String) extends StringField(v) {
}

class WordInfo (val name: String,  val value: Json)

object WordLib extends Schema with CustomTypesMode {
  val wordsLib = table[WordInfo]("WordInfo")
}
