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

import scala.concurrent.{Future}
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

    val futureResponse: Future[List[PermutationResult]] = {
      val system = ActorSystem("SystemActor")
      implicit val timeout = Timeout(timeoutPeriod seconds)

      val playActor = system.actorOf(Props[PlayActor], name = "PlayActor")
      ask(playActor, ProcessWords(wordsRequest.words)).mapTo[List[PermutationResult]]
    }

    futureResponse.map(wordMap => {
      // converting result to JsObject
      var resultJsObject: JsObject = JsObject(Seq.empty)
      wordMap.map(x => resultJsObject = x.permutation ++ resultJsObject)
      Ok(resultJsObject)
    })
  }
}
