package actors

import akka.actor.{Actor, ActorLogging, ActorPath}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import play.common.{PermutationRequest, PermutationResult}

import scala.concurrent.{Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.pipe

case class ProcessWords(words: Seq[String])

class PlayActor extends Actor with ActorLogging{

  val initialContacts = Set(ActorPath.fromString(ConfigFactory.load().getString("clusterAddress")))
  val client = context.system.actorOf(
    ClusterClient.props(
      ClusterClientSettings(context.system)
        .withInitialContacts(initialContacts)),
    "PlayAppClient")

  def receive = {
    case request: ProcessWords ⇒ {

      val timeoutPeriod = ConfigFactory.load().getLong("timeoutPeriod")
      implicit val timeout = Timeout(timeoutPeriod seconds)

      val workerSeq =
        request.words.map(word => {
          ask(client, ClusterClient.Send(
            "/user/workerAppClusterListener",
            new PermutationRequest(word),
            localAffinity = true))
            .mapTo[PermutationResult]
        })

      Future.sequence(workerSeq)
        .mapTo[List[PermutationResult]] pipeTo sender()
    }
  }
}