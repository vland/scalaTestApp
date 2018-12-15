package play.common

import play.api.libs.json.JsObject

case class PermutationRequest(word: String) {}
case class PermutationResult(id: String, permutation: JsObject) {}
