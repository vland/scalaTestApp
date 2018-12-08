import db.DbAdapter
import db.entity.WordInfo

object WorkerApp extends App{
  println("Worker App started")

  val dbAdapter = new DbAdapter
  val name = "milk4";
  var result: WordInfo = null
  var searchResult = dbAdapter.findWord(name)
  if (searchResult != None) {
    result = searchResult.get
  } else {
    // TODO: calculate permutation
    var permutation = "namePermutation"
    dbAdapter.insertWord(name, permutation)
    result = new WordInfo(name, permutation)
  }

  println(s"Result: ${result.name}, ${result.value}")

  println("Worker App ended")
}
