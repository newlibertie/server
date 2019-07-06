package com.newlibertie.pollster.api.v1

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object Disqualification {
  /**
    * REST api
    * Disqualify a Voter (restricted)
    * Qualify a Voter : Needed only for a disqualified voter (restricted)
    */
  lazy val routes:Route = {
      path("disqualification") {
        get {
          parameters("id") { (id: String) =>
            complete(
              HttpEntity(
                ContentTypes.`application/json`,
                "{}"
              )
            )
          }
        } ~
        post {
          entity(as[String]) {
            pollDefinition => complete(s"received ${pollDefinition}")
          }
        }
      }
    }
}
