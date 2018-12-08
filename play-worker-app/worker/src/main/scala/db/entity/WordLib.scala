package db.entity

import org.squeryl.Schema

class WordInfo (val name: String,  val value: String) {
}

object WordLib extends Schema{
  val wordsLib = table[WordInfo]("WordInfo")
}
