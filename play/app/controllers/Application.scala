package controllers

import actors.{PlayActor, ProcessWords}
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import javax.inject.Inject
import models.WordsRequest
import play.api.libs.json._
import play.api.mvc._
import play.common.PermutationResult

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action {
    Ok("Call POST /getWords method with request like { \"values\" : [\"milk\", \"house\"]} to permutate all words")
  }

  def getWords = Action.async {implicit request =>

    val json = request.body.asJson.get
    val wordsRequest = json.as[WordsRequest]
    val timeoutPeriod = ConfigFactory.load().getLong("timeoutPeriod")

    val futureResponse: Future[List[PermutationResult]] = Future {
      val system = ActorSystem("SystemActor")

      implicit val timeout = Timeout(timeoutPeriod seconds)

      val playActor = system.actorOf(Props[PlayActor], name = "PlayActor")
      val actorCall = playActor ? ProcessWords(wordsRequest.words)

      val result = Await.result(actorCall, timeout.duration).asInstanceOf[List[PermutationResult]]

      result
    }

    futureResponse.map(wordMap => {
      // converting result to JsObject
      var resultJsObject: JsObject = JsObject(Seq.empty)
      wordMap.map(x => resultJsObject = x.permutation ++ resultJsObject)
      Ok(resultJsObject)
    })
  }
}
