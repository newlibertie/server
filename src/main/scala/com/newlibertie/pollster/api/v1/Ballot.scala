package com.newlibertie.pollster.api.v1

import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post}
import akka.http.scaladsl.server.Route

object Ballot {
  /**
    * REST api
    * Cast a Ballot
    */
  lazy val routes:Route = {
      path("ballot") {
        post {
          entity(as[String]) {
            ballot => complete(s"received ${ballot}")
          }
        }
      }
    }

}
