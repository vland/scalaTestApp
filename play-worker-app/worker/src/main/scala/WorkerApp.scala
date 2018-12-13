import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.client.ClusterClientReceptionist
import com.typesafe.config.ConfigFactory
import db.DbAdapter
import db.entity.{Json, WordInfo}
import play.api.libs.json._

object WorkerApp extends App{

  def startNodes(ports: Seq[String]): Unit = {
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString(s"""akka.remote.netty.tcp.port=$port""").withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)

      // Create an actor that handles cluster domain events
      val workerService = system.actorOf(Props[WorkerAppListener], name = "workerAppClusterListener")
      ClusterClientReceptionist(system).registerService(workerService)
    }
  }

  if(args.isEmpty) {
    startNodes(Seq("2551", "2552", "0"))
  } else {
    startNodes(args)
  }
}

class WorkerAppListener extends Actor with ActorLogging {
  var senderActor : ActorRef = null;

  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive = {
    case (item: PermutationRequest) => {
      val res = findWord(item.word)
      sender() ! new PermutationResult(item.word, res)
    }
    //case MemberUp(member) =>
      //log.info("Member is Up: {}", member.address)
    //case UnreachableMember(member) =>
      //log.info("Member detected as unreachable: {}", member.address)
    //case MemberRemoved(member, previousStatus) =>
      //log.info("Member is removed: {}", member.address, previousStatus)
  }

  private def findWord(id: String): JsObject = {

    var result: JsObject = null
    val dbAdapter = new DbAdapter
    var searchResult = dbAdapter.findWord(id)
    if (searchResult != None) {
      result = searchResult.get.value
    } else {
      val jsPermutation = JsObject(Seq(id -> Json.toJson(id.permutations.toArray)))
      dbAdapter.insertWord(new WordInfo(id, jsPermutation))
      result = jsPermutation
    }
    result
  }

  // implicit convertions for custom type
  implicit def jsObjectToJson(jsObject: JsObject): Json = new Json(jsObject.toString)
  implicit def jsonToJsObject(json: Json): JsObject = Json.parse(json.value).asInstanceOf[JsObject]
}