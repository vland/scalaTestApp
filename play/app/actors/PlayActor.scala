package actors

import akka.actor.{Actor, ActorLogging, ActorPath}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.pattern.ask
import akka.util.Timeout

import com.typesafe.config.ConfigFactory
import play.common.{PermutationRequest, PermutationResult}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Success, Failure }

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

      var finalResult: List[PermutationResult] = List()

      val workerSeq = Future.sequence(
        request.words.map(word => {
          Future {
            val clientCall = client ? ClusterClient.Send(
              "/user/workerAppClusterListener",
              new PermutationRequest(word),
              localAffinity = true)
            val result = Await.result(
              clientCall,
              timeout.duration)
              .asInstanceOf[PermutationResult]

            // Adding result to a list
            finalResult = result :: finalResult

            result
            }
        })
      )

      workerSeq.onComplete{
        case Success(response) => {
          log.info("all results received")
        }
        case Failure(ex) => {
          throw ex
        }
      }

      Await.ready(workerSeq, timeoutPeriod seconds)
      sender() ! finalResult
    }
  }
}