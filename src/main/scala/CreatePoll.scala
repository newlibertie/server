import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, concat, get, pathPrefix}
import akka.stream.ActorMaterializer

import scala.collection.mutable

class CreatePoll {

  implicit val system = ActorSystem("pollster")
  implicit val materializer = ActorMaterializer()

  lazy val routes = {
    pathPrefix("v1") {
      pathPrefix("poll") {
        get {
          complete(
            HttpEntity(
              ContentTypes.`application/json`,
              "{}"
            )
          )
        }
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(routes, "0.0.0.0", 1959)

  implicit val executionContext = system.dispatcher

  def stop(): Unit = {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}