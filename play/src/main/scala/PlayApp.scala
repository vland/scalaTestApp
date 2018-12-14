import akka.actor.{Actor, ActorLogging, ActorPath, ActorSystem, Props}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import com.typesafe.config.ConfigFactory
import play.api.libs.json.{JsObject, Json}

case class ProcessWords(words: Seq[String])

object PlayApp extends App {
  val system = ActorSystem("PlayApp", ConfigFactory.load())

  val request = "{ \"values\" : [\"test3\", \"test4\", \"test1\", \"test2\", \"test5\"] }"

  var jsRequest = Json.parse(request).asInstanceOf[JsObject]

  if(jsRequest.keys("values")) {
    val words = jsRequest.value("values").as[Seq[String]]
    val playApp = system.actorOf(Props[PlayActor], name = "PlaySender")
    playApp ! ProcessWords(words)
  } else {
    println("There is no words to search")
  }
}

class PlayActor extends Actor with ActorLogging{

  var elementsToProcess: Int = 0
  var elementsProcessed: Int = 0
  var wordMap: scala.collection.mutable.Map[String, JsObject] = scala.collection.mutable.Map[String, JsObject]()

  val initialContacts = Set(ActorPath.fromString(ConfigFactory.load().getString("clusterAddress")))
  val client = context.system.actorOf(ClusterClient.props(ClusterClientSettings(context.system).withInitialContacts(initialContacts)), "PlayAppClient")

  def receive = {
    case request: ProcessWords ⇒ {
      elementsProcessed = 0
      elementsToProcess = request.words.length
      wordMap = scala.collection.mutable.Map[String, JsObject]()
      request.words.foreach(word => {
        client ! ClusterClient.Send("/user/workerAppClusterListener", new PermutationRequest(word), localAffinity = true)
      })
    }
    case result: PermutationResult => {
      //log.info(s"Answer from cluster: ${result.id} : ${result.permutation}")

      if(!wordMap.contains(result.id)) {
        wordMap.put(result.id, result.permutation)
      }
      elementsProcessed += 1

      if(elementsProcessed == elementsToProcess) {
        var resultJsObject: JsObject = JsObject(Seq.empty)
        wordMap.values.map(x => resultJsObject = x ++ resultJsObject)
        println(s"End result of permutations: ${resultJsObject.toString}")
      }
    }
  }
}