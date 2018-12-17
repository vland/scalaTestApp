package controllers

import actors.{PlayActor, ProcessWords}
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import models.WordsRequest
import play.api.libs.json._
import play.api.mvc._
import play.common.PermutationResult
import javax.inject._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class Application @Inject()(system: ActorSystem,
                            cc: ControllerComponents)
  extends AbstractController(cc) {

  val logger = LoggerFactory.getLogger(classOf[Application])

  def index = Action {
    Ok("Call POST /getWords method with request like { \"values\" : [\"milk\", \"house\"]} to permutate all words")
  }

  def getWords = Action.async {implicit request =>

    val json = request.body.asJson.get
    val wordsRequest = json.as[WordsRequest]
    val timeoutPeriod = ConfigFactory.load().getLong("timeoutPeriod")
    val playActor = system.actorOf(Props[PlayActor], name = "PlayActor")

    try{
      val futureResponse: Future[List[PermutationResult]] = {
        implicit val timeout = Timeout(timeoutPeriod seconds)
        ask(playActor, ProcessWords(wordsRequest.words)).mapTo[List[PermutationResult]]
      }

      futureResponse.map(wordMap => {
        // converting result to JsObject
        val resultJsObject: JsObject = wordMap.foldLeft(JsObject(Seq.empty)) {(res, item) => item.permutation ++ res}

        // stopping other actors
        playActor ! "Stop"
        Ok(resultJsObject)
      })
    } catch {
      case e: Throwable => {
        playActor ! "Stop"
        logger.error(e.getMessage, e)
        throw e
      }
    }
  }
}
