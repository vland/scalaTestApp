package models

case class WordsRequest(words: Seq[String])


object WordsRequest {

  import play.api.libs.json._

  implicit object WordsRequestFormat extends Format[WordsRequest] {

    def reads(json: JsValue): JsResult[WordsRequest] = {
      val words = (json \ "values").as[Seq[String]]
      JsSuccess(WordsRequest(words))
    }

    def writes(w: WordsRequest): JsValue = {
      // JsObject requires Seq[(String, play.api.libs.json.JsValue)]
      val wordsRequest = Seq("values" -> JsArray(w.words.map(JsString(_))))
      JsObject(wordsRequest)
    }
  }
}