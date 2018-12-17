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

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class Application @Inject()(system: ActorSystem,
                            cc: ControllerComponents)
  extends AbstractController(cc) {

  def index = Action {
    Ok("Call POST /getWords method with request like { \"values\" : [\"milk\", \"house\"]} to permutate all words")
  }

  def getWords = Action.async {implicit request =>

    val json = request.body.asJson.get
    val wordsRequest = json.as[WordsRequest]
    val timeoutPeriod = ConfigFactory.load().getLong("timeoutPeriod")
    val playActor = system.actorOf(Props[PlayActor], name = "PlayActor")

    val futureResponse: Future[List[PermutationResult]] = {

      implicit val timeout = Timeout(timeoutPeriod seconds)

      val result = ask(playActor, ProcessWords(wordsRequest.words)).mapTo[List[PermutationResult]]
      result
    }

    futureResponse.map(wordMap => {
      // converting result to JsObject
      var resultJsObject: JsObject = JsObject(Seq.empty)
      wordMap.map(x => resultJsObject = x.permutation ++ resultJsObject)

      // stopping other actors
      playActor ! "Stop"
      Ok(resultJsObject)
    })
  }
}
