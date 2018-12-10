import akka.actor.{Actor, ActorLogging, ActorPath, ActorSystem, Props}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import com.typesafe.config.ConfigFactory

case class ProcessWord(word: String)

object PlayApp extends App {
  val system = ActorSystem("PlayApp", ConfigFactory.load())

  val playApp = system.actorOf(Props[PlayActor], name = "PlaySender")
  playApp ! ProcessWord("milk")
}

class PlayActor extends Actor with ActorLogging{

  val initialContacts = Set(ActorPath.fromString(ConfigFactory.load().getString("clusterAddress")))
  val client = context.system.actorOf(ClusterClient.props(ClusterClientSettings(context.system).withInitialContacts(initialContacts)), "PlayAppClient")

  def receive = {
    case in: ProcessWord ⇒
      client ! ClusterClient.Send("/user/workerAppClusterListener", in.word, localAffinity = true)
    case e => {
      log.info(s"Answer from cluster: $e")
    }
  }
}